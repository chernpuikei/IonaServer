import java.util.ArrayList;

/**
 * Created on 2017/11/22.
 */
class Map {

    private String[][] world;
    static final int MS = 70;
    static final int MR = MS/2;
    private static final int MOUNT_TO_GENERATE = 3;
    private static final double PERCENTAGE = (double) MOUNT_TO_GENERATE/MS;

    Map() {
        this.world = new String[MS][MS];
        basicTerrain();
        checkDP(world);
        river(mount());
        Log.showTotal(true,world);
    }

    private void basicTerrain() {
        for (int hor = 0;hor<MS;hor++) {
            for (int j = 0;j<MS;j++) {
                double centralDis = centralDistance(hor,j);
                world[hor][j] = basicTypeByDistance(centralDis);
            }
        }
    }

    //todo:根据半径距离将所有方格归结为水、沙、绿地、可山绿地、起始地
    private String basicTypeByDistance(double cd) {//cd for CentralDistance
        int hms = MS/2-2;//Half Map Size
        return cd<hms? cd<hms*0.9? cd<hms*0.65? cd<hms*0.05? "@": "-": "#": "*": "?";
    }

    //todo:计算出当前点的中心距离
    private double centralDistance(int x,int y) {
        int deltaX = Math.abs(x-MS/2), deltaY = Math.abs(y-MS/2);
        return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
    }

    //todo:从绿地中随机产生mountain
    private ArrayList<int[]> generateMountain() {
//        i("generateMountain>>");
        float ptt = 0.002f; int availableCount = 1;
        ArrayList<int[]> mountOrigins = new ArrayList<>();
        //todo:iterate every single cord with two for-statement
        for (int ver = 0;ver<MS;ver++) {
            for (int hor = 0;hor<MS;hor++) {
                if (world[hor][ver].equals("-") && Math.random()<ptt
                      && availableCount-->0) {//triggers
                    world[hor][ver] = "^";//turn
//                    i("turn mount",hor+","+ver);
                    mountOrigins.add(new int[]{hor,ver});
                    //todo:height
                    int curHei = (int) (5*Math.random()+5);//first height
//                    i("mount height",curHei);
                    for (int y = ver-curHei;y<ver;y++) {//height on hor
                        world[hor][y] = "^";
                    }
                    //todo:width
                    int r = (int) ((Math.random()+0.5)*curHei);//then width
//                    i("mount r",r);
                    for (int x = hor-r;x<hor+r;x++) {//width on ver
                        world[x][ver] = "^";
                    }
                    //todo:left
                    double[] formula1 = Bezier.getFormula(
                          new double[]{hor,ver-curHei},new double[]{hor-r,ver});
                    for (int x = hor-r;x<hor;x++) {
                        double temp = getBound(formula1,x);
                        int curBound = (int) temp;
                        for (int y = curBound;y<ver;y++) {
                            world[x][y] = "^";
                        }
                    }
                    //todo:right
                    double[] formula2 = Bezier.getFormula(
                          new double[]{hor,ver-curHei},new double[]{hor+r,ver});
                    for (int x = hor+1;x<=hor+r;x++) {
                        double temp = getBound(formula2,x);
                        int curBound = (int) temp;
                        for (int y = curBound;y<ver;y++) {
                            world[x][y] = "^";
                        }
                    }
                }
            }
        }
        //todo:mark the origin mount point to generate river
        for (int[] cur : mountOrigins) {
            world[cur[0]][cur[1]] = "~";
        }
        return mountOrigins;
    }

    private ArrayList<int[]> mount() {
//        Log.i("mountBuild>>");
        int[] o = new int[]{MR,MR};//原点
        ArrayList<int[]> mountOri = new ArrayList<>();
        for (int i = 0;i<3;i++) {//迭代xs的同时也在迭代象限
            Log.i("mount generating",i);
            int r = (int) (Math.random()*MR*0.6+MR*0.2);//半径100以外400以内
            Log.i(2,"r",r);
            int[] ranX = getRanX(i,r), ranY = getRanY(i,r);
            int x = ranX[0]+((int) (Math.random()*(ranX[1]-ranX[0])));
            int[] ys = generateYs(o,r,x);
            int y = pickY(ys,ranY);
            mountOri.add(new int[]{x,y});
            world[y][x] = "^";
//            Log.i(4,"final X/Y/R",x+"/"+y+"/"+r);
//            Log.i(4,"(x-500)^2+(y-500)^2",(x-o[0])*(x-o[0])+(y-o[1])*(y-o[1]));
//            Log.i(4,"r*r",r*r);
        }
        Log.iiArrayList("mountBuilt",mountOri);
        Log.showTotal(true,world);
        return mountOri;
    }

    private int[] getRanX(int area,int r) {
        switch (area) {
            case 0:
                return new int[]{MR-r,MR};
            case 1:
                return new int[]{MR,MR+r};
            default:
                int hello = (int) (Math.sqrt(3)/2*r);
                return new int[]{MS/2-hello,MS/2+hello};
        }
    }

    private int[] getRanY(int area,int r) {
        switch (area) {
            case 0:
            case 1:
                return new int[]{MS/2-r,MS/2+r/2};
            default:
                return new int[]{MS/2,MS/2+r};
        }
    }

    private int pickY(int[] ys,int[] yRange) {
        boolean[] checkList = new boolean[2];
        for (int i = 0;i<ys.length;i++) {
            checkList[i] = ys[i]>yRange[0] && ys[i]<yRange[1];
        }
        int result = checkList[0] && checkList[1]? Math.random()>0.5? ys[0]: ys[1]:
              checkList[0]? ys[0]: ys[1];
//        Log.i(4,"pickY>>");
//        Log.c(8,"yRange",yRange);
//        Log.c(8,"ys",ys);
//        Log.i(8,"result",result);
        return result;
    }

    private int[] generateYs(int[] o,int radius,int x) {
//        Log.i(2,"generateYs>>");
        int root = (int) (Math.sqrt(radius*radius-(x-o[0])*(x-o[0])));
        int[] result = new int[]{MS/2+root,MS/2-root};
//        Log.i(4,"x:"+x); Log.i(4,"radius",radius); Log.c(4,"o{}",o);
//        Log.i(4,"root@gYs()",root);
//        Log.c(4,"result@gYs()",result);
        return result;
    }

    private int[] generateXRsThroughSSs(int[][] sss) {//只是在指定范围内产生了x，半径未明确
        int[] result = new int[3];
        for (int i = 0;i<sss.length;i++) {
            result[i] = (int) (sss[i][0]+Math.random()*sss[i][1]);
        }
        return result;
    }

    private int[][] getSSs(int[][] ranges) {
        int[][] result = new int[3][2];
        for (int i = 0;i<ranges.length;i++) {
            result[i] = getSS(ranges[i]);
        }
        return result;
    }

    private int[] getSS(int[] curRange) {//Start & Space
        return new int[]{curRange[0],curRange[1]-curRange[0]};
    }

    private int looper(int curArea) {
        return (curArea+3-1)%3;
    }

    private void river(ArrayList<int[]> mountOri) {
        Log.i("river>>");
        Integer[] total = new Integer[]{0,1,2};
        for (int i = 0;i<mountOri.size();i++) {
            int[] curOri = mountOri.get(i);
            Integer[] ri = pickTheRest(total,i);//for rest index
            double[] formula = Bezier.getFormula(
                  mountOri.get(ri[0]),mountOri.get(ri[1]));//余下两点连成的线段的方程
            double[] bound = calBound(mountOri.get(ri[0]),mountOri.get(ri[1]));
            boolean lor = leftOrRight(curOri,formula);
            int[] des = genDes(i,lor,formula[0]>0,bound,mountOri.get(i));

            world[des[0]][des[1]] = i+"";
//            ArrayList<Integer[]> river = new Bezier(mountOri.get(i),des).getArrayList();
//            for (Integer[] pixel : river) {
//                world[pixel[1]][pixel[0]] = "~";
//            }
        }
    }

    private static void checkDP(String[][] world) {//check Divide Point
        int r = MS/2;
        ArrayList<int[]> check = new ArrayList<>();
        double hello = Math.sqrt(3)/2, hi = r*1.5;
        check.add(new int[]{r,0});
        check.add(new int[]{(int) ((1-hello)*r),(int) hi});
        check.add(new int[]{(int) ((1+hello)*r),(int) hi});
        for (int i = 0;i<check.size();i++) {
            int[] cur = check.get(i);
            world[cur[0]][cur[1]] = "D";
        }
        Log.iiArrayList(0,"dividePoint",check);
    }

    private static int[] genDes(int counter,boolean lor,boolean pon,double[] bounds,int[] mount) {
        //bounds:{上下左右}
        Log.i(4,"genDes>>",counter);
        boolean hov = Math.random()>0.5, same = same(pon,hov);
        double takBou = bounds[hov? lor? 2: 3: same? 1: 0];//四选一
        Log.i(6,"bound taken",takBou);
        double[] range = new double[]{//上面已经选过，这里只是决定takBou是作为起点还是终点
              hov? lor? 0: takBou: same? takBou: 0,
              hov? lor? takBou: MS-1: same? MS-1: takBou};
        Log.c(6,"range?",range);
        double ranSpa = range[1]-range[0], //计算出随机空间
              taken = range[0]+Math.random()*ranSpa;//计算出随机x|y
        Log.i(6,"ranSpa",ranSpa);
        Log.i(6,"range[0]+random()(range[1]-range[0])");
        double[] xy4yx = oneAnotherCycle(taken);//随机坐标投射到圆上得出y|x(两个)
        Log.c(6,"xy4yx(2)",xy4yx);
        int[][] psb = new int[2][2];//PosSiBle
        for (int i = 0;i<2;i++) {//分别算出两个结果距离当前mountOri的长度，取长度较长的一个
            psb[i][0] = (int) (hov? xy4yx[i]: taken);
            psb[i][1] = (int) (hov? taken: xy4yx[i]);
        }
        int[] result =
              psb[Bezier.calDis(mount,psb[0])>Bezier.calDis(mount,psb[1])? 0: 1];
        Log.c(6,"des generated",result);
        Log.i(4,"<<genDes");
        return result;

    }

    private static double[] oneAnotherCycle(double cur) {
        Log.i("onAnotherCycle>>>"); Log.i(2,"input x|y",cur);
        double r = MS/2, dx = Math.abs(cur-r);
        double check = Math.sqrt(r*r-dx*dx);
        double[] result = new double[]{r+check,r-check};
        Log.c(2,"possible result",result); Log.i("<<<onAnotherCycle");
        return result;
    }

    private static boolean same(boolean pon,boolean hov) {
        return (pon && hov) | (!pon && !hov);
    }

    private static double[] calBound(double[] p0,double[] p1) {
        boolean hor = p0[0]<p1[0], ver = p0[1]<p1[1];
        return new double[]{hor? p0[0]: p1[0],hor? p1[0]: p0[0],
              ver? p0[1]: p1[1],ver? p1[1]: p0[1]};
    }

    private static double[] calBound(int[] p0,int[] p1) {
        boolean hor = p0[0]<p1[0], ver = p0[1]<p1[1];
        return new double[]{hor? p0[0]: p1[0],hor? p1[0]: p0[0],
              ver? p0[1]: p1[1],ver? p1[1]: p0[1]};
    }

    private static boolean leftOrRight(double[] curOri,double[] othFor) {
        double xToCompare = curOri[0]*othFor[0]+othFor[1];
        return curOri[1]<xToCompare;
    }

    private static boolean leftOrRight(int[] curOri,double[] othFor) {
        double xToCompare = curOri[0]*othFor[0]+othFor[1];
        return curOri[1]<xToCompare;
    }

    private Integer[] pickTheRest(Integer[] total,int cur) {
        Integer[] result = new Integer[]{0,0};
        int resultCounter = 0;
        for (int i = 0;i<total.length;i++) {
            if (total[i]!=cur) {
                result[resultCounter++] = total[i];
            }
        }
        return result;
    }

    private int[] invalidate(int[] curPos,String curMove) {
        //二维数组中的第一个元素指示垂直位置，第二个元素指示水平位置
        int posOnVer = curPos[0], posOnHor = curPos[1];
        Log.i(2,"curPos about to invalidate",posOnHor+"/"+posOnVer);
        Log.i(4,"curMove",curMove);
        switch (curMove) {
            case "left":
                posOnHor--; break;
            case "right":
                posOnHor++; break;
            case "up":
                posOnVer--; break;
            case "down":
                posOnVer++; break;
        }
        Log.i(6,"invalidated",posOnHor+"/"+posOnVer);
        return new int[]{posOnVer,posOnHor};//返回格式符合数组中的表达逻辑
    }

    private boolean rewrites(int[] curPos) {
        if (!world[curPos[0]][curPos[1]].equals("?")) {
            world[curPos[0]][curPos[1]] = "~";//当前数组元素不为'?'表示未到尽头
            return false;
        } else {
            return true;
        }
    }

    private String opposite(String cur) {
        switch (cur) {
            case "left":
                return "right";
            case "right":
                return "left";
            case "up":
                return "down";
            case "down":
                return "up";
            default:
                return "error";
        }
    }

    private static String[] excludeDir(String[] dirsLeft,String dirToExclude) {
        Log.i("excludeDir>>");
        Log.array("para-dirsLeft",dirsLeft,dirsLeft.length);
        Log.i("dirToExclude",dirToExclude);
        ArrayList<String> excluded = new ArrayList<>();
        for (String curToCheck : dirsLeft) {
            if (!curToCheck.equals(dirToExclude)) {
                excluded.add(curToCheck);
            }
        }
        String[] excludedInArray = new String[excluded.size()];
        for (int i = 0;i<excluded.size();i++) {
            excludedInArray[i] = excluded.get(i);
        }
        //todo:并未确定什么时候停止exclude
        Log.array("excluded",excludedInArray,excludedInArray.length);
        Log.i("<<excludeDir");
        return excludedInArray;
    }

    private void findTheWay(int[] curPosition) {//重新找到河流生成方法？
        switch ((int) (Math.random()/0.33)) {
            case 0:
                break;
            case 1:
        }
    }

    //todo:获取特定类型的方格的数目
    private int getTypeCount(String typeToGet) {
        int counter = 0;
        for (int i = 0;i<world.length;i++) {
            for (int j = 0;j<world[i].length;j++) {
                if (world[i][j].equals(typeToGet)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    static boolean sameOnHor(float[] p0,float[] p1) {
        return p0[0]==p1[0];
    }

    private double getBound(double[] formula,int x) {
        return formula[0]*x+formula[1];
    }

}
