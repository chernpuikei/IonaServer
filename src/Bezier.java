import java.util.ArrayList;

/**
 * Created on 2017/12/1.
 */
class Bezier extends ArrayList<Integer[]> {//在外部先生成bezier的起终点，获取bezier

    private ArrayList<Integer[]> all = new ArrayList<>();
    private int[] control, mp;
    static final int ppe = 50;

    Bezier(int[] sia,int[] eia) {//需要保证sta是河流起点，end
        Log.i(4,"bezier()>>"); Log.c(6,"sia",sia); Log.c(6,"eia",eia);
        double[] start = new double[]{sia[0]*ppe,sia[1]*ppe},
              end = new double[]{eia[0]*ppe,eia[1]*ppe};
        double[] control = initControl(start,end);
        Log.c(6,"start",start); Log.c(6,"end",end); Log.c(6,"control",control);
        double totalDis = DoMath.calDis(start,end);
        for (int i = 0;i<totalDis;i += 10) {
            double toPro = i/totalDis;
            double[] p1 = DoMath.calMP(start,control,toPro),
                  p2 = DoMath.calMP(control,end,toPro), p3 = DoMath.calMP(p1,p2,toPro);
            int x2Add = (int) (p3[0]/ppe), y2Add = (int) (p3[1]/ppe);
            if (all.size()!=0) {//一般情况，获取目录前一位如果不同则添加当前
                Integer[] previous = all.get(all.size()-1);
                if (x2Add!=previous[0] && y2Add!=previous[1] && all.size()!=0) {
                    Integer[] check;
                    all.add(check = new Integer[]{x2Add,y2Add});
                    Log.c(6,"to add",check);
                }
            } else {//起始阶段添加起点，
                Integer[] check =
                      new Integer[]{(int) (start[0]/ppe),(int) (start[1]/ppe)};
                all.add(check);
                Log.c(6,"to add",check);
            }
        }

        Log.i(4,"<<<Bezier");
    }

    ArrayList<Integer[]> getArrayList() {//整条bezier的getter
        return this.all;
    }

    int[] getControl() {//control的getter
        return this.control;
    }

    int[] getMP() {//MP的getter
        return this.mp;
    }

    private boolean sameOnWhich(float[] mOri,float[] con) {//soe for Start Or End
        return mOri[0]==con[0];
    }

    private double[] initControl(double[] start,double[] end) {//产生控制点
        double[] forStaEnd = DoMath.getFormula(start,end);//Formula Start End
        double cenX = (start[0]+end[0])/2, cenY = (start[1]+end[1])/2;
        double verLineA = -1/forStaEnd[0], verLineB = cenY-cenX*verLineA;
        double seDis = DoMath.calDis(start,end)/2;//取起终点距离的一半作为控制点
        //todo:解二次方程
        double[] xs = DoMath.caaD(
              new double[]{verLineA,verLineB},new double[]{cenX,cenY},seDis);
        boolean dirToGo = !(end[0]-start[0]>0);//1 for right;0 for left
        //todo:取同侧control
        double conX = xs[dirToGo? 0: 1], conY = verLineA*conX+verLineB;
        this.control = new int[]{(int) (conX/ppe),(int) (conY/ppe)};
        this.mp = new int[]{(int) (cenX/ppe),(int) (cenY/ppe)};
        //todo:在world中标记处conX和conY
        Log.i(4,"initControl>>");
        Log.c(6,"start",start);
        Log.c(6,"end",end);//起终点
        Log.i(6,"start,end==> cenX,cenY",cenX+","+cenY);//起终点的中点
        Log.c(6,"start,end==> forStaEnd",forStaEnd);//起终点连线的方程
        Log.i(6,"verLineA",verLineA);
        Log.i(6,"verLineB",verLineB);//垂线方程
        Log.i(6,"start,end==> seDis",seDis);//起终点距离
        Log.i(6,"conX,conY to return",conX+","+conY);//由上述距离在垂线方程中找出两个点
        Log.i(4,"<<initControl");
        return new double[]{conX,conY};
    }

}
