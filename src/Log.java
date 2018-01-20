import java.util.ArrayList;

/**
 * Created on 2018/1/5.
 */
class Log {

    static void array(String header,String[] array,int cpg) {
        int groupCount = array.length/cpg;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<cpg;j++) {
                currentLine += array[i*cpg+j]+(j==cpg-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+\n";
        System.out.print(finalPrint);
    }

    static void array(String header,float[] array,int cpg) {
        int groupCount = array.length/cpg;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<cpg;j++) {
                currentLine += array[i*cpg+j]+(j==cpg-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+\n";
        System.out.print(finalPrint);
    }

    static void array(String header,Integer[] array,int cpg) {
        int groupCount = array.length/cpg;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<cpg;j++) {
                currentLine += array[i*cpg+j]+(j==cpg-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+\n";
        System.out.print(finalPrint);
    }

    static void array(int tabCount,String header,Integer[] array,int cpg) {
        int groupCount = array.length/cpg;
        String tab = generateTabs(tabCount);
        String finalPrint = tab+"+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = tab+"|"+i+"(";
            for (int j = 0;j<cpg;j++) {
                currentLine += array[i*cpg+j]+(j==cpg-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += tab+"+\n";
        System.out.print(finalPrint);
    }

    static void al(String header,ArrayList<Integer[]> check) {
        String total = "+"+header+"\n";
        for (Integer[] aCheck : check) {
            String cur = "|";
            for (float aCurArray : aCheck) {
                cur += aCurArray+"|";
            }
            total += cur+"\n";
        }
        System.out.print(total);
    }

    static void i(String message) {
        System.out.print(message+"\n");
    }

    static void i(String key,Object value) {
        System.out.print(key+":"+value+"\n");
    }

    static void iArrayList(int tabs,String header,ArrayList<Integer[]> hi) {
        i(2,header);
        for (Integer[] singleCord : hi) {
            c(4,singleCord);
        }
        i(2,"<<");
    }

    static void c(int tabCount,Integer[] cordToShow) {
        System.out.print(generateTabs(tabCount)+"("+cordToShow[0]+","+cordToShow[1]+")\n");
    }

    static void c(int tabCount,int[] cordToShow) {
        System.out.print(generateTabs(tabCount)+"("+cordToShow[0]+","+cordToShow[1]+")\n");
    }

    static void c(String cordName,double[] cord) {
        System.out.print(cordName+":("+cord[0]+","+cord[1]+")\n");
    }

    static void c(int tabs,String cordName,double[] cord) {
        System.out.print(generateTabs(tabs)+cordName+":("+cord[0]+","+cord[1]+")\n");
    }

    static void c(String cordName,int[] cord) {
        System.out.print(cordName+":("+cord[0]+","+cord[1]+")\n");
    }

    static void c(int tc,String cordName,int[] cord) {
        System.out.print(generateTabs(tc)+cordName+":("+cord[0]+","+cord[1]+")\n");
    }

    static void c(int tc,String cordName,Integer[] cord) {
        System.out.print(generateTabs(tc)+cordName+":("+cord[0]+","+cord[1]+")\n");
    }


    static void ii(String message) {
        System.out.print(message);
    }

    static void i(int tabs,String message) {
        System.out.print(generateTabs(tabs)+message+"\n");
    }

    static void i(int tabs,String key,Object value) {
        System.out.print(generateTabs(tabs)+key+":"+value+"\n");
    }

    static String generateTabs(int count) {
        String check = "";
        for (int i = 0;i<count;i++) {
            check += " ";
        }
        return check;
    }

    static void array(String header,Object[] array,int cpg) {
        System.out.print("+"+header+"+\n");
        int groups = array.length/cpg;
        for (int i = 0;i<groups;i++) {
            String curLine = "+";
            for (int j = 0;j<cpg;j++) {
                curLine += array[i*cpg+j]+",";
            }
            curLine += "\n";
            System.out.print(curLine);
        }
    }

    static void iiArrayList(int tab,String header,ArrayList<int[]> target) {
        i(tab,header);
        for (int i = 0;i<target.size();i++) {
            c(tab+2,target.get(i));
        }
    }

    static void iiArrayList(String header,ArrayList<int[]> target) {
        iiArrayList(0,header,target);
    }

    static void showTotal(boolean mergeByDifference,String[][] world) {
        if (mergeByDifference) alterTheScene(world,calChangeList(world));
        for (int y = 0;y<world.length;y++) {
            for (int x = 0;x<world[y].length;x++) {
                ii(world[x][y]);
            }
            ii("\n");
        }
    }

    static boolean[][] calChangeList(String[][] world) {
        //todo:make another array to compare whether or not to change
        boolean[][] whetherToChange = new boolean[world.length][world.length];
        for (int i = 0;i<world.length;i++) {
            for (int j = 0;j<world[i].length;j++) {
                whetherToChange[i][j] = allTheSame(new int[]{i,j},world) |
                      world[i][j].equals("-") | world[i][j].equals("#") |
                      world[i][j].equals("?");
            }
        }
        return whetherToChange;
    }

    static void alterTheScene(String[][] world,boolean[][] toChange) {
        for (int i = 0;i<world.length;i++) {
            for (int j = 0;j<world[i].length;j++) {
                if (toChange[i][j]
                      && !world[i][j].equals("~") && !world[i][j].equals("^")) {
                    world[i][j] = " ";
                }
            }
        }
    }

    static boolean allTheSame(int[] cur,String[][] world) {
        if (cur[0]-1<0 | cur[1]-1<0 |
              cur[0]+1==Map.MS | cur[1]+1==Map.MS)
            return false;//if current index is on the edge of the map,don't change
        String curString = world[cur[0]][cur[1]];
        int[] hor = new int[]{cur[0]-1,cur[0]+1},
              ver = new int[]{cur[1]-1,cur[1]+1};
        boolean allTheSame = true;
        for (int i = 0;i<hor.length && allTheSame;i++) {//如果allTheSame中途变成false
            allTheSame = curString.equals(world[hor[i]][cur[1]]);
        }
        for (int i = 0;i<ver.length && allTheSame;i++) {//那么它将不会有机会变回来
            allTheSame = curString.equals(world[cur[0]][ver[i]]);
        }
        return allTheSame;
    }

}
