package kdtree;

import java.util.ArrayList;

public class UtilZ {
    /**
     * �������ά�ȵķ���
     * 
     * @param data      ����
     * @param dimention ά��
     * @return ����
     */
    static double variance(ArrayList<double[]> data, int dimention) {
        double vsum = 0;
        double sum = 0;
        for (double[] d : data) {
            sum += d[dimention];
            vsum += d[dimention] * d[dimention];
        }
        int n = data.size();
        return vsum / n - Math.pow(sum / n, 2);
    }

    /**
     * ȡ�������м�λ����ֵ
     * 
     * @param data      ����
     * @param dimention ά��
     * @return
     */
    static double median(ArrayList<double[]> data, int dimention) {
        double[] d = new double[data.size()];
        int i = 0;
        for (double[] k : data) {
            d[i++] = k[dimention];
        }
        return findPos(d, 0, d.length - 1, d.length / 2);
    }

    static double[][] maxmin(ArrayList<double[]> data, int dimentions) {
        double[][] mm = new double[2][dimentions];
        // ��ʼ�� ��һ��Ϊmin���ڶ���Ϊmax
        for (int i = 0; i < dimentions; i++) {
            mm[0][i] = mm[1][i] = data.get(0)[i];
            for (int j = 1; j < data.size(); j++) {
                double[] d = data.get(j);
                if (d[i] < mm[0][i]) {
                    mm[0][i] = d[i];
                } else if (d[i] > mm[1][i]) {
                    mm[1][i] = d[i];
                }
            }
        }
        return mm;
    }

    static double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return sum;
    }

    /**
     * ��max��min��ʾ�ĳ������еĵ�͵�a����С����
     * 
     * @param a   ��a
     * @param max �����θ���ά�ȵ����ֵ
     * @param min �����θ���ά�ȵ���Сֵ
     * @return �������еĵ�͵�a����С����
     */
    static double mindistance(double[] a, double[] max, double[] min) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max[i])
                sum += Math.pow(a[i] - max[i], 2);
            else if (a[i] < min[i]) {
                sum += Math.pow(min[i] - a[i], 2);
            }
        }

        return sum;
    }

    /**
     * ʹ�ÿ������򣬲��������λ����point����ֵ
     * ��Array.sort()��ȥ��Ӧλ��ֵ����Լ��30%
     * 
     * @param data  ����
     * @param low   �μ��������͵�
     * @param high  �μ��������ߵ�
     * @param point λ��
     * @return
     */
    private static double findPos(double[] data, int low, int high, int point) {
        int lowt = low;
        int hight = high;
        double v = data[low];
        ArrayList<Integer> same = new ArrayList<Integer>((int) ((high - low) * 0.25));
        while (low < high) {
            while (low < high && data[high] >= v) {
                if (data[high] == v) {
                    same.add(high);
                }
                high--;
            }
            data[low] = data[high];
            while (low < high && data[low] < v)
                low++;
            data[high] = data[low];
        }
        data[low] = v;
        int upper = low + same.size();
        if (low <= point && upper >= point) {
            return v;
        }

        if (low > point) {
            return findPos(data, lowt, low - 1, point);
        }

        int i = low + 1;
        for (int j : same) {
            if (j <= low + same.size())
                continue;
            while (data[i] == v)
                i++;
            data[j] = data[i];
            data[i] = v;
            i++;
        }

        return findPos(data, low + same.size() + 1, hight, point);
    }
}
