import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created on 16/3/1.
 */
public class Main {

    public static void main(String args[]) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        new Map();
        startListening();
    }

    private static void testMethod() {
        System.out.print("testMethod>>>\n");
        String[][] check = new String[][]{new String[]{"a","b","c","d","e"},
              new String[]{"f","g","h","i","j"}};
        for (int i = 0;i<2;i++) {
            String curToBeShown = "";
            for (int j = 0;j<5;j++) {
                curToBeShown += check[i][j];
            }
            curToBeShown += "\n";
            System.out.print(curToBeShown);
        }
    }

    private static void startListening() {
        try {
            ServerSocket serversocket = new ServerSocket(20001);
            while (true) {
                Socket socket = serversocket.accept();
                new Thread(new RespondThread(socket)).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] getDate() {
        String[] t0 = new Date().toString().split(" ");
        String[] t1 = new String[]{t0[5],t0[1],t0[2]};
        return new int[]
              {Integer.parseInt(t1[0]),transMon(t1[1]),Integer.parseInt(t1[2])};
    }

    private static int transMon(String month) {
        switch (month) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "Mar":
                return 3;
            case "Apr":
                return 4;
            case "May":
                return 5;
            case "Jun":
                return 6;
            case "Jul":
                return 7;
            case "Aug":
                return 8;
            case "Sep":
                return 9;
            case "Oct":
                return 10;
            case "Nov":
                return 11;
            case "Dec":
                return 12;
            default:
                return 0;
        }
    }

}
