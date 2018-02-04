
/**
 * Created on 2018/1/31.
 */
public class DoMath {

    static double[] calMP(double[] start,double[] end,double progress) {
        double deltaX = end[0]-start[0], deltaY = end[1]-start[1];
        return new double[]{start[0]+deltaX*progress,start[1]+progress*deltaY};
    }

    //todo:点X线X距得出结果
    //Cord At Appoint Distance
    static double[] caaD(double[] fl,double[] point,double dis) {
        double f_vse_a = fl[0], f_vse_b = fl[1], cenX = point[0], cenY = point[1];
        double qf_a = f_vse_a*f_vse_a+1, qf_b = 2*((f_vse_b-cenY)*f_vse_a-cenX),
              qf_c = (f_vse_b-cenY)*(f_vse_b-cenY)+cenX*cenX-dis*dis;

        return rQF(qf_a,qf_b,qf_c);
    }

    static double[] rQF(double a,double b,double c) {
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
        double[] result = new double[]{a,b};
        Log.i("getFormula>>");
        Log.c("para.p0",p0); Log.c("para.p1",p1); Log.c("result",result);
        return result;
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

    static double[] oneAnotherCycle(double cur) {
        double r = Map.MR, dx = Math.abs(cur-r);
        double check = Math.sqrt(r*r-dx*dx);
        return new double[]{r+check,r-check};
    }

    //todo:计算出当前点的中心距离
    static double centralDistance(int x,int y) {
        int deltaX = Math.abs(x-Map.MS/2), deltaY = Math.abs(y-Map.MS/2);
        return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
    }

}
