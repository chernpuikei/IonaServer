import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Date;

/**
 * Created on 16/3/1.
 */
public class Main {

    private static Socket socket;
    private static final String dbURL = "jdbc:mysql://localhost:3306/IonaServer";

    public static void main(String args[]) {


        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ServerSocket serversocket = new ServerSocket(2000);
            System.out.print("server socket started");
            while (true) {
                socket = serversocket.accept();
                new Thread(new RespondThread()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e){
//            e.printStackTrace();
//        }

    }

    private static class RespondThread implements Runnable {

        Connection connection;
        Statement statement;

        @Override
        public void run() {
            System.out.print(Thread.currentThread() + "\n");
            try {
                JsonObject jsonObject = Json.createReader(new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))).readObject();
                System.out.print(Thread.currentThread() + "jsonObject:" + jsonObject + "\n");
                try {
                    connection = DriverManager.getConnection(dbURL, "root", "aslcnm18");
                    statement = connection.createStatement();
                    String requestType = jsonObject.getString("requestType");
                    String email = jsonObject.getString("email");

                    switch (requestType) {
                        case "check": //第一种请求,判断登陆或注册
                            System.out.print("caseCheck\n");
                            ResultSet rs = statement.executeQuery(
                                    "SELECT * from user_information WHERE email = \'"
                                            + jsonObject.getString("email") + "\'");
                            answer("respondType", requestType, "value", rs.next() + "");
                            break;
                        case "lor":
                            //服务器接到lor请求时需要先判断到底是注册请求还是登录请求
                            if (statement.executeQuery(
                                    "SELECT * from user_information WHERE email = \'"
                                            + jsonObject.getString("email") + "\'").next()) {
                                ResultSet login = statement.executeQuery(
                                        "SELECT * from user_information WHERE email = \'"
                                                + jsonObject.getString("email")
                                                + "\'and password=\'"
                                                + jsonObject.getString("poc") + "\'");
                                answer("respondType", requestType, "value", login.next() + "");
                            } else {
                                answer("respondType", requestType, "value", !statement.execute(
                                        "INSERT into user_information VALUES (\'" + email + "\',\'"
                                                + jsonObject.getString("poc") + "\')") + "");
                            }
                            break;
                        case "record":
                            JsonArray jsonArray = jsonObject.getJsonArray("AdditionalData");
                            System.out.print("jsonArray:" + jsonArray + "\n");
                            break;
                        case "path":
                            statement.execute("insert into path_saver (email,num,staX,staY," +
                                    "endX,endY,c1X,c1Y,c2X,c2Y,width,height) values('"
                                    + email + "'," + jsonObject.getInt("num") + ","
                                    + jsonObject.getString("staX") + ","
                                    + jsonObject.getString("staY") + ","
                                    + jsonObject.getString("endX") + ","
                                    + jsonObject.getString("endY") + ","
                                    + jsonObject.getString("c1X") + ","
                                    + jsonObject.getString("c1Y") + ","
                                    + jsonObject.getString("c2X") + ","
                                    + jsonObject.getString("c2Y") + ","
                                    + jsonObject.getInt("width") + ","
                                    + jsonObject.getInt("height") + ");");
                            break;
                        case "footprint":
                            JsonArray pos_tan = jsonObject.getJsonArray("pos_tan");
                            for (int i = 0; i < pos_tan.size(); i++) {
                                JsonArray oneStep = pos_tan.getJsonArray(i);
                                statement.execute("insert into pt_saver (email,pos_x,pos_y,"
                                        + "tan1,tan2,s_i,p_i) values ('" + email + "',"
                                        + oneStep.getJsonNumber(0) + ","
                                        + oneStep.getJsonNumber(1) + ","
                                        + oneStep.getJsonNumber(2) + ","
                                        + oneStep.getJsonNumber(3) + ","
                                        + jsonObject.getInt("s_i") + ","
                                        + i + ");");

                            }
                            break;
                        case "content":
                            String[] date = new Date().toString().split(" ");
                            BufferedWriter bw = new BufferedWriter(new FileWriter(
                                    jsonObject.getString("email") + date[5] + date[1] + date[2]
                                            + ".txt"));
                            bw.write(jsonObject.getString("content"));
                            bw.flush();
                            break;
                        case "request_content":
                            String[] date1 = new Date().toString().split(" ");
                            BufferedReader br = new BufferedReader(new FileReader(
                                    jsonObject.getString("email") + date1[5] + date1[1] + date1[2]
                                            + ".txt"));
                            String temp = br.readLine();
                            answer("respondType", requestType, "value", temp);
                            System.out.print("temp is " + temp);
                            break;

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int translate(String mon) {
            String[] temp = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Seq", "Oto", "Nov", "Dec"};

            for (int i = 0; i < 12; i++) {
                if (temp[i].equals(mon)) {
                    return i + 1;
                }
            }
            return 0;
        }
    }

    private static void answer(String key1, String value1, String key2, String value2)
            throws IOException {
        JsonWriter jsonWriter = Json.createWriter(socket.getOutputStream());
        jsonWriter.write(Json.createObjectBuilder().add(key1, value1).add(key2, value2).build());
        System.out.print(key1 + "//" + value1 + "//" + key2 + "//" + value2 + "\n");
        jsonWriter.close();
    }
}
