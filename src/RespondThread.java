import javax.json.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;

class RespondThread implements Runnable {

    private final static String queryWithES =
            "select * from %s where email = '%s' and s_i = %d";
    private final static String getQueryWithESP =
            "select * from %s where email = '%s' and s_i = %d and p_i = %d";
    private final static String checkEmail =
            "select * from user_information where email = '%s'";
    private final static String checkPassword =
            "select * from user_information where email = '%s' and password = '%s'";
    private final static String regis =
            "insert into user_information(email,password,nickname,intro,width,height) values('%s','%s','%s','%s',%d,%d)";
    private final static String insertPath =
            "insert into path_saver (email,s_i,staX,staY,endX,endY,e1x,e1y,e2x,e2y,year,month) values('%s',%d,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d)";
    private final static String insertPath2 =
            "insert into path_saver (email,s_i,staX,staY,endX,endY,e1x,e1y,e2x,e2y,e3x,e3y,year,month) values('%s',%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d)";
    private final static String insertFootprints =
            "insert into pt_saver (email,pos_x,pos_y,tan1,tan2,s_i,p_i) values('%s',%f,%f,%f,%f,%d,%d)";

    private Socket socket;

    RespondThread(Socket socket) {this.socket = socket;}

    @Override

    public void run() {
        try {
            got(socket);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void got(Socket socket) throws IOException, SQLException {

        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/IonaServer","root","aslcnm18");
        Statement statement = connection.createStatement();
        JsonObject joj = Json.createReader(socket.getInputStream()).readObject();
        System.out.print(">>"+joj+"\n");
        String requestType = joj.getString("requestType");
        String email = joj.getString("email"), sql, password;
        int si, pi, year, month, day;
        JsonNumber sx, sy, ex, ey, e1x, e1y, e2x, e2y, e3x, e3y;
        JsonObjectBuilder rBuilder = Json.createObjectBuilder();
        rBuilder.add("requestType",requestType);
        switch (requestType) {
            case "check": //第一种请求,判断登陆或注册
                sql = String.format(checkEmail,email);
                ResultSet rs_check = statement.executeQuery(sql);
                boolean record_existed = rs_check.next();
                rBuilder.add("existed",record_existed);
                break;
            case "login":
                password = joj.getString("password");
                sql = String.format(checkPassword,email,password);
                ResultSet login = statement.executeQuery(sql);
                boolean password_correct = login.next();
                rBuilder.add("correct",password_correct);
                break;
            //merge user_info & register & initScreenSize
            case "register":
                password = joj.getString("password");
                int width = joj.getInt("width"),
                        height = joj.getInt("height");
                String nickName = joj.getString("nickName");
                String selfIntro = joj.getString("selfIntro");
                sql = String.format(regis,email,password,nickName,selfIntro,width,height);
                boolean succeeded = !statement.execute(sql);
                rBuilder.add("succeeded",succeeded);
                break;
            case "checkWidthHeight":
                int width_check = joj.getInt("width"),
                        height_check = joj.getInt("height");
                sql = String.format(checkEmail,email);
                ResultSet rs_cwh = statement.executeQuery(sql);
                rs_cwh.next();
                rBuilder.add("result",rs_cwh.getInt("width")==width_check
                        && rs_cwh.getInt("height")==height_check);
                break;
            case "initContent"://根据当前日期
                //用用户名+年月日拼接处存储在服务器端的用户名
                System.out.print("about to initContent>\n");
                si = joj.getInt("si"); pi = joj.getInt("pi");
                String filename_ic = String.format("%s%d%d.txt",email,si,pi);
                System.out.print("filename generated:"+filename_ic+"\n");
                BufferedWriter bw = new BufferedWriter(new FileWriter(filename_ic));
                String finalInput = joj.getString("province")
                        +"#"+joj.getString("city")+"#"+joj.getString("content");
                bw.write(finalInput);
                bw.flush();
                break;
            case "initCanvas":
                si = joj.getInt("si");
                JsonArray path = joj.getJsonArray("path"),
                        posTan = joj.getJsonArray("posTan");
                sx = path.getJsonNumber(0); sy = path.getJsonNumber(1);
                ex = path.getJsonNumber(2); ey = path.getJsonNumber(3);
                e1x = path.getJsonNumber(4); e1y = path.getJsonNumber(5);
                e2x = path.getJsonNumber(6); e2y = path.getJsonNumber(7);
                //当前canvas si为0的时候
                sql = si-1==-1? String.format(checkEmail,email):
                        String.format(queryWithES,"path_saver",email,si-1);
                ResultSet rs_date = statement.executeQuery(sql);
                rs_date.next();
                int year_ic = rs_date.getInt("year"),
                        month_ic = rs_date.getInt("month");
                //两段注释之间是为了初始化时间
                if (path.size()==10) {
                    e3x = path.getJsonNumber(8); e3y = path.getJsonNumber(9);
                    sql = String.format(insertPath2,email,si,f(sx),f(sy),f(ex),f(ey),f(e1x),f(e1y),f(e2x),f(e2y),f(e3x),f(e3y),year_ic,month_ic);
                } else {
                    sql = String.format(insertPath,email,si,f(sx),f(sy),f(ex),f(ey),f(e1x),f(e1y),f(e2x),f(e2y),year_ic,month_ic);
                }
                statement.execute(sql);
                int size_ic = posTan.size()/4;
                for (int i = 0;i<size_ic;i++) {
                    sql = String.format(insertFootprints,email,
                            posTan.getJsonNumber(4*i).doubleValue(),
                            posTan.getJsonNumber(4*i+1).doubleValue(),
                            posTan.getJsonNumber(4*i+2).doubleValue(),
                            posTan.getJsonNumber(4*i+3).doubleValue(),
                            si,i);
                    statement.execute(sql);
                }
                break;
            case "requestCanvas":
                System.out.print("requestCanvas>>\n");
                si = joj.getInt("si");
                sql = String.format(queryWithES,"path_saver",email,si);
                System.out.print("sql 2b execute:"+sql+"\n");
                ResultSet rs_path = statement.executeQuery(sql);
                boolean recordExisted = rs_path.next();
                rBuilder.add("canvas_exist",recordExisted);
                if (recordExisted) {
                    //先写path
                    JsonArrayBuilder builder_path = Json.createArrayBuilder();
                    builder_path.add(rs_path.getFloat("staX"))
                            .add(rs_path.getFloat("staY"))
                            .add(rs_path.getFloat("endX"))
                            .add(rs_path.getFloat("endY"))
                            .add(rs_path.getFloat("e1x"))
                            .add(rs_path.getFloat("e1y"))
                            .add(rs_path.getFloat("e2x"))
                            .add(rs_path.getFloat("e2y"))
                            .add(rs_path.getFloat("e3x"))
                            .add(rs_path.getFloat("e3y"));
                    rBuilder.add("path",builder_path.build());
                    //再写date
                    JsonArrayBuilder builder_date = Json.createArrayBuilder();
                    sql = String.format(queryWithES,"path_saver",email,si);
                    ResultSet rs_rc_date = statement.executeQuery(sql);
                    rs_rc_date.next();
                    builder_date.add(rs_rc_date.getInt("year"))
                            .add(rs_rc_date.getInt("month"));
                    rBuilder.add("date",builder_date.build());
                    //最后写footprints
                    JsonArrayBuilder builder_fps = Json.createArrayBuilder();
                    sql = String.format(queryWithES,"pt_saver",email,si);
                    ResultSet rs_fps = statement.executeQuery(sql);
                    while (rs_fps.next()) {
                        builder_fps.add(rs_fps.getFloat("pos_x"))
                                .add(rs_fps.getFloat("pos_y"))
                                .add(rs_fps.getFloat("tan1"))
                                .add(rs_fps.getFloat("tan2"));
                    }
                    rBuilder.add("posTan",builder_fps.build());
                } else {
                    System.out.print("record not exist>>\n");
                    //todo:当si=0时，返回(0,0)作为preDes
                    //todo:先试试不区分si等不等于0
                    //todo:不等于0:e3x=0&&e3y=0 >> e2x=0&&e2y=0
                    float[] pre_des;
                    if (si!=0) {
                        System.out.print("requestCanvas=false & si!=0\n");
                        //todo:查询的record not exist则转而查询上一个si所表示的月份从而得出
                        //todo:本条si所代表的月份
                        sql = String.format(queryWithES,"path_saver",email,--si);
                        System.out.print("SQL 2b execute @requestCanvas:"+sql+"\n");
                        //todo:先获取e3x,e3y,如果两者都为0,返回e2x,e2y
                        ResultSet rs_des = statement.executeQuery(sql);
                        rs_des.next();
                        float pre_e3x = rs_des.getInt("e3x"),
                                pre_e3y = rs_des.getInt(("e3y")),
                                pre_e2x = rs_des.getInt("e2x"),
                                pre_e2y = rs_des.getInt("e2y");
                        boolean fourOrThree = pre_e3x!=0 && pre_e3y!=0;
                        //todo:如果查明是有四个象限，则一律返回第四个象限的终点，
                        //todo:如果是只有三个象限，则一律返回第三个象限的终点
                        //todo:在上述情况下，如果si=0，取上一si得到的resultSet结果应该是0？
                        pre_des = fourOrThree?
                                new float[]{pre_e3x,pre_e3y}:
                                new float[]{pre_e2x,pre_e2y};
//                        ResultSet rs_ymd = statement.executeQuery(sql);
//                        rs_ymd.next();
//                        int year_rs_ymd = rs_ymd.getInt("year"),
//                                month_rs_ymd = rs_ymd.getInt("month");
//                        rBuilder.add("year",year_rs_ymd).add("month",month_rs_ymd);
                    } else {
                        pre_des = new float[]{0,0};
                    }
                    JsonArrayBuilder jab_des = Json.createArrayBuilder();
                    jab_des.add(pre_des[0]).add(pre_des[1]);
                    JsonArray ja_des = jab_des.build();
                    rBuilder.add("preDes",ja_des);
                    break;//如果一个屏幕的FP需要新开，那么content记录是必然不存在的
                }
            case "requestContent"://日期由客户端发送,{"reqDay":int[3]}
                System.out.print("requestContent>>\n");
                //todo:1.拼接文件名|2.读取文件|3.将读取到的信息添加到rBuilder里
                si = joj.getInt("si"); pi = joj.getInt("pi");
                boolean content_exist = true;
                try {
                    String filename = email+si+pi+".txt";
                    System.out.print("file to open:"+filename+"\n");
                    BufferedReader br = new BufferedReader(new FileReader(filename));
                    String rContent = "", temp;
                    //todo-b
                    while ((temp = br.readLine())!=null) {
                        rContent += temp;
                    }
                    rBuilder.add("content",rContent);
                    sql = String.format(queryWithES,"path_saver",email,si);
                    ResultSet rs_rc = statement.executeQuery(sql); rs_rc.next();
                    year = rs_rc.getInt("year"); month = rs_rc.getInt("month");
                    rBuilder.add("year",year).add("month",month);
                } catch(FileNotFoundException f) {
                    //todo-c
                    System.out.print("file not found");
                    content_exist = false;
                } finally {
                    //todo-d
                    rBuilder.add("content_exist",content_exist);
                    rBuilder.add("si",si).add("pi",pi);
                }
                break;
            case "clearDB":
                statement.execute("delete from pt_saver");
                statement.execute("delete from path_saver");
                break;
        }
        JsonWriter jw = Json.createWriter(socket.getOutputStream());
        JsonObject oj = rBuilder.build();
        System.out.print("<<"+oj+"\n");
        //jw.write(rBuilder.build());
        jw.write(oj);

        jw.close();
        socket.close();
    }

    private void show(String string) {
        System.out.print(string+"\n");
    }

    private int[] getProperDate(int[] oldDate) {
        int year = oldDate[0], month = oldDate[1], day = oldDate[2];
        int curMonDay = 30;
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                curMonDay = 31;
                break;
            case 2:
                curMonDay = year%4==0? 29: 28;
                break;
        }
        day++;
        if (day>curMonDay) {  //+1的日期后大于当月最大值
            day = 1;
            if ((month++)>12) {
                year++;
                month = 1;
            }
        }
        return new int[]{year,month,day};
    }

    private float f(JsonNumber jsonNumber) {
        return Float.parseFloat(jsonNumber+"");
    }

}