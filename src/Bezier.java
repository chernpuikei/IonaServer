import java.util.ArrayList;

/**
 * Created on 2017/12/1.
 */
class Bezier extends ArrayList<Integer[]> {

    private ArrayList<Integer[]> all = new ArrayList<>();
    static final int ppe = 50;

    Bezier(int[] sia,int[] eia) {//需要保证sta是河流起点，end
        Log.i(4,"bezier()>>"); Log.c(6,"sia",sia); Log.c(6,"eia",eia);
        double[] start = new double[]{sia[0]*ppe,sia[1]*ppe},
              end = new double[]{eia[0]*ppe,eia[1]*ppe};
        double[] control = getControl(start,end);
        Log.c(6,"start",start); Log.c(6,"end",end); Log.c(6,"control",control);
        double totalDis = calDis(start,end);
        for (int i = 0;i<totalDis;i += 10) {
            double toPro = i/totalDis;
            double[] p1 = getMP(start,control,toPro), p2 = getMP(control,end,toPro),
                  p3 = getMP(p1,p2,toPro);
            int x2Add = (int) (p3[0]/ppe), y2Add = (int) (p3[1]/ppe);
            if (all.size()!=0) {
                Integer[] previous = all.get(all.size()-1);
                if (x2Add!=previous[0] && y2Add!=previous[1] && all.size()!=0) {
                    Integer[] check;
                    all.add(check = new Integer[]{x2Add,y2Add});
                    Log.c(6,"to add",check);
                }
            } else {//起始阶段添加起点
                Integer[] check =
                      new Integer[]{(int) (start[0]/ppe),(int) (start[1]/ppe)};
                all.add(check);
                Log.c(6,"to add",check);
            }
        }

        Log.i(4,"<<<Bezier");
    }

    ArrayList<Integer[]> getArrayList() {
        return this.all;
    }

    private boolean sameOnWhich(float[] mOri,float[] con) {//soe for Start Or End
        return mOri[0]==con[0];
    }

    private double[] getControl(double[] start,double[] end) {
        double[] forStaEnd = getFormula(start,end);//Formula Start End
        double cenX = (start[0]+end[0])/2, cenY = (start[1]+end[1])/2;
        double verLineA = -1/forStaEnd[0], verLineB = cenY-cenX*verLineA;
        double seDis = calDis(start,end)/2;//取起终点距离的一半作为控制点
        //todo:解二次方程
        double[] xs = caaD(new double[]{verLineA,verLineB},
              new double[]{cenX,cenY},seDis);
        boolean dirToGo = !(end[0]-start[0]>0);//1 for right;0 for left
        double conX = xs[dirToGo? 0: 1], conY = verLineA*conX+verLineB;

        Log.i(4,"getControl>>");
        Log.c(6,"start",start);
        Log.c(6,"end",end);//起终点
        Log.i(6,"start,end==> cenX,cenY",cenX+","+cenY);//起终点的中点
        Log.c(6,"start,end==> forStaEnd",forStaEnd);//起终点连线的方程
        Log.i(6,"verLineA",verLineA);
        Log.i(6,"verLineB",verLineB);//垂线方程
        Log.i(6,"start,end==> seDis",seDis);//起终点距离
        Log.i(6,"conX,conY to return",conX+","+conY);//由上述距离在垂线方程中找出两个点
        Log.i(4,"<<getControl");
        return new double[]{conX,conY};
    }

    private double[] getMP(double[] start,double[] end,double progress) {
        double deltaX = end[0]-start[0], deltaY = end[1]-start[1];
        return new double[]{start[0]+deltaX*progress,start[1]+progress*deltaY};
    }

    //todo:点X线X距得出结果
    //Cord At Appoint Distance
    private static double[] caaD(double[] fl,double[] point,double dis) {
        double f_vse_a = fl[0], f_vse_b = fl[1], cenX = point[0], cenY = point[1];
        double qf_a = f_vse_a*f_vse_a+1,
              qf_b = 2*((f_vse_b-cenY)*f_vse_a-cenX),//Quadratic Function
              qf_c = (f_vse_b-cenY)*(f_vse_b-cenY)+cenX*cenX-dis*dis;

        return rQF(qf_a,qf_b,qf_c);
    }

    private static double[] rQF(double a,double b,double c) {
        //todo:根据一条二次方多项式的a,b,c算出此多项式的两个解
        double temp = Math.sqrt(b*b-4*a*c),
              preRes1 = (-b+temp)/(a*2), preRes2 = (-b-temp)/(a*2);
        return new double[]{preRes1,preRes2};
    }

    static double calDis(float[] central,float[] compare) {
        float deltaX = central[0]-compare[0], deltaY = central[1]-compare[1];
        return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
    }


    static double[] getFormula(double[] p0,double[] p1) {
        Log.i(2,"getFormula>>"); Log.c(4,"p0",p0); Log.c(4,"p1",p1);
        double a = (p1[1]-p0[1])/(p1[0]-p0[0]);
        double b = p1[1]-a*p1[0];
        return new double[]{a,b};
    }

    static double[] getFormula(int[] p0,int[] p1) {
        double a = (p1[1]-p0[1])/(p1[0]-p0[0]);
        double b = p1[1]-a*p1[0];
        return new double[]{a,b};
    }

    static double oneAnother(double para,double[] formula,boolean xOy) {
        return xOy? formula[0]*para+formula[1]: (para-formula[1])/formula[0];
    }

    static double calDis(int[] central,int[] compare) {
        int deltaX = central[0]-compare[0], deltaY = central[1]-compare[1];
        return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
    }

    static double calDis(double[] central,double[] compare) {
        double deltaX = central[0]-compare[0], deltaY = central[1]-compare[1];
        return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
    }


}
