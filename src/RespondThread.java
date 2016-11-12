import javax.json.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;

class RespondThread implements Runnable {

   private Socket socket;
   private int[] today;

   RespondThread(Socket socket,int[] today) {
      this.socket = socket;
      this.today = today;
   }

   @Override
   public void run() {
      try {
         got(socket);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void got(Socket socket) throws SQLException, IOException {

      Connection connection = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/IonaServer","root","aslcnm18");
      Statement statement = connection.createStatement();

      JsonObject joj = Json.createReader(new BufferedReader(
            new InputStreamReader(socket.getInputStream()))).readObject();
      System.out.print(joj+"\n");
      String requestType = joj.getString("requestType");
      String email = joj.getString("email");

      switch (requestType) {
         case "check": //第一种请求,判断登陆或注册
            ResultSet rs_0 = statement.executeQuery(
                  "SELECT * from user_information WHERE email = \'"
                        +joj.getString("email")+"\'");
            String temp00 = rs_0.next()+"";
            reply(socket,"respondType",requestType,"value",temp00);
            break;
         case "initScreenSize":
            int width = joj.getInt("width"), height = joj.getInt("height");
            statement.execute("update user_information set width ="+width
                        +",height ="+height+" where email ='"+email+"';");
            break;
         case "user_info":
            statement.execute(
                  "insert into user_information set nickname = '"
                        +joj.getString("nickname")+"',self_intro ='"
                        +joj.getString("self_intro")+"';");
            break;
         case "login":
            ResultSet login = statement.executeQuery(
                  "SELECT * from user_information WHERE email = \'"
                        +joj.getString("email")+"\'and password=\'"
                        +joj.getString("poc")+"\'");
            String r_email = login.next() ? login.getString("email") : "nah";
            reply(socket,"respondType",requestType,"value",r_email);
            break;
         case "register":
            boolean temp_r = !statement.execute(
                  "INSERT into user_information(email,password,nickname,intro) VALUES (\'"
                        +email+"\',\'"+joj.getString("password")+"\',\'"
                        +joj.getString("nickname")+"\',\'"+joj.getString("self_intro")
                        +"\')");
            reply(socket,"respondType",requestType,"value",temp_r+"");
            break;
         case "record":
            JsonArray jsonArray = joj.getJsonArray("AdD");
            break;
         case "path":
            //inserting path record into path_saver
            statement.execute(
                  "insert into path_saver (email,s_i,staX,staY,endX,endY,c1X,c1Y,c2X,c2Y) values(\'"
                        +email+"\',"+joj.getInt("s_i")+","+joj.getJsonNumber("staX")
                        +","+joj.getJsonNumber("staY")+","+joj.getJsonNumber("endX")
                        +","+joj.getJsonNumber("endY")+","+joj.getJsonNumber("c1X")
                        +","+joj.getJsonNumber("c1Y")+","+joj.getJsonNumber("c2X")
                        +","+joj.getJsonNumber("c2Y")+");");
            reply(socket,"respondType",requestType,"value","true");
            break;
         case "writing_footprint"://根据当前日期
            JsonArray pos_tan = joj.getJsonArray("pos_tan");
            int s_i = joj.getInt("s_i")-1;
            log(s_i);
            int[] date;
            ResultSet rsp = statement.executeQuery(
                  "select year,month,day from pt_saver where email =\'"+email+"\' and s_i ="+s_i);
            if (rsp.last()) {
               date = getProperDate(
                     rsp.getInt("year"),rsp.getInt("month"),rsp.getInt("day"));
            } else {
               date = today;
            }
            for (int i = 0;i < pos_tan.size();i++) {
               JsonArray oneStep = pos_tan.getJsonArray(i);
               statement.execute(
                     "insert into pt_saver (email,pos_x,pos_y,tan1,tan2,s_i,p_i,year,month,day) values ('"+email+"',"+oneStep.getJsonNumber(0)+","+oneStep.getJsonNumber(1)+","+oneStep.getJsonNumber(2)+","+oneStep.getJsonNumber(3)+","+joj.getInt("s_i")+","+i+","+date[0]+","+date[1]+","+date[2]+");");
               date = getProperDate(date[0],date[1],date[2]);
            }
            reply(socket,"respondType",requestType,"value","true");
            break;
         case "content"://根据当前日期
            //用用户名+年月日拼接处存储在服务器端的用户名
            System.out.print("today:"+today[0]+today[1]+today[2]+"\n");
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                  joj.getString("email")+today[0]+today[1]+today[2]+".txt"));
            bw.write(joj.getString("content"));
            bw.flush();
            break;
         case "request_content"://日期由客户端发送,{"reqDay":int[3]}
            //用用户名+年月日拼接处存储在服务器端的用户名
            ResultSet rc = statement.executeQuery(
                  "select year,month,day from pt_saver where email = '"+email+"' and s_i ="+joj.getInt("s_i")+" and p_i="+joj.getInt("p_i")+";");
            rc.next();//这里有且只有一行，既不需要考虑空集问题也不需要考虑多行情况
            int year = rc.getInt("year"), month = rc.getInt("month"),
                  day = rc.getInt("day");
            int date_cal = year*10000+month*100+day;
            int today_cal = today[0]*10000+today[1]*100+today[2];
            String time = date_cal > today_cal ? "tomorrow"
                  : date_cal == today_cal ? "today" : "yesterday";
            String temp = "";

            JsonArrayBuilder jb = Json.createArrayBuilder();
            try {
               BufferedReader br = new BufferedReader(new FileReader(email
                     +rc.getInt("year")+rc.getInt("month")+rc.getInt("day")+".txt"));
               while ((temp = br.readLine()) != null) {
                  jb.add(temp);
               }
//          temp = temp == null ? "nah" : temp;
            } catch (FileNotFoundException e) {
               System.out.print("Record not exist!!!\n");
//          temp = "nah";
            }
            JsonArray result = jb.build();
            JsonWriter jw = Json.createWriter(socket.getOutputStream());
            jw.write(Json.createObjectBuilder().add("respondType",requestType)
                  .add("time",time).add("value",result).build());
            jw.close();
            break;
         case "request_footprints":
            int si = joj.getJsonNumber("which_path").intValue();
            ResultSet rs = statement.executeQuery(
                  "select * from pt_saver inner join path_saver on path_saver.s_i = pt_saver.s_i and pt_saver.email = \'"+email+"\' and pt_saver.s_i="+si);
            JsonArrayBuilder builder = Json.createArrayBuilder();
            boolean posTanEmpty = true;
            boolean pathReady = false;
            float staX = 0, staY = 0, endX = 0, endY = 0, c1X = 0, c1Y = 0, c2X = 0,
                  c2Y = 0;

            while (rs.next()) {
               if (!pathReady) {
                  staX = rs.getFloat("staX"); staY = rs.getFloat("staY");
                  endX = rs.getFloat("endX"); endY = rs.getFloat("endY");
                  c1X = rs.getFloat("c1X"); c1Y = rs.getFloat("c1Y");
                  c2X = rs.getFloat("c2X"); c2Y = rs.getFloat("c2Y");
                  pathReady = true;
               }
               posTanEmpty = false;
               JsonObject footprint = Json.createObjectBuilder()
                     .add("posX",rs.getFloat("pos_x")).add("posY",rs.getFloat("pos_y"))
                     .add("tan1",rs.getFloat("tan1")).add("tan2",rs.getFloat("tan2"))
                     .add("year",rs.getInt("year")).add("month",rs.getString("month"))
                     .add("day",rs.getInt("day")).build();
               builder.add(footprint);
            }
            JsonArray footprints = builder.build();
            JsonObjectBuilder rJB = Json.createObjectBuilder()
                  .add("respondType",requestType).add("existed",!posTanEmpty);
            ResultSet rs_date = statement.executeQuery(
                  "select year,month,day from pt_saver where email ='"+email+"'and s_i= "+si+" and p_i=0");
            if (rs_date.next()) {
               JsonArrayBuilder jab = Json.createArrayBuilder();
               jab.add(rs_date.getInt("year")).add(rs_date.getInt("month"))
                     .add(rs_date.getInt("day"));
               rJB.add("date",jab.build());
            }

            ResultSet ss = statement.executeQuery(
                  "select width,height from user_information where email =\'"+email+"\'");
            int width_t = 0;
            int height_t = 0;
            if (ss.next()) {
               width_t = ss.getInt("width");
               height_t = ss.getInt("height");
            }
            if (!posTanEmpty)
               rJB.add("footprints",footprints).add("staX",staX).add("staY",staY)
                     .add("endX",endX).add("endY",endY).add("c1X",c1X).add("c1Y",c1Y)
                     .add("c2X",c2X).add("c2Y",c2Y)
                     .add("width",width_t).add("height",height_t);
            JsonObject resJ = rJB.build();
            System.out.print("respondJSON:"+resJ.getString("respondType")+"\n");
            BufferedWriter bufferedWriter = new BufferedWriter(
                  new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(resJ+"");
            bufferedWriter.flush();
            bufferedWriter.close();
      }
      socket.close();
   }

   private static void reply(Socket socket,String key1,String value1,
         String key2,String value2) throws IOException {
      JsonWriter jsonWriter = Json.createWriter(socket.getOutputStream());
      jsonWriter.write(
            Json.createObjectBuilder().add(key1,value1).add(key2,value2).build());
      System.out.print("replying:"+key1+":"+value1+","+key2+":"+value2+"\n");
      jsonWriter.close();
   }

   private int[] getProperDate(int year,int month,int day) {

      int curMonDay = 30;
      switch (month) {
         case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            curMonDay = 31;
            break;
         case 2:
            curMonDay = year%4 == 0 ? 29 : 28;
            break;
      }
      day++;
      if (day > curMonDay) {  //+1的日期后大于当月最大值
         day = 1;
         if ((month++) > 12) {
            year++;
            month = 1;
         }
      }
      return new int[]{year,month,day};
   }

   static void log(Object string) {
      System.out.print(string+"\n");
   }

}