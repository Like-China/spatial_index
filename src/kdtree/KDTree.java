package kdtree;

import java.util.ArrayList;
import java.util.Stack;

/**
 * ���ڲ�����query point�����point
 */
public class KDTree {
    private Node kdtree;

    private KDTree() {
    }

    /**
     * ������
     * 
     * @param input ����
     * @return KDTree��
     */
    public static KDTree build(double[][] input) {
        int n = input.length;
        int m = input[0].length;

        ArrayList<double[]> data = new ArrayList<double[]>(n);
        for (int i = 0; i < n; i++) {
            double[] d = new double[m];
            for (int j = 0; j < m; j++)
                d[j] = input[i][j];
            data.add(d);
        }

        KDTree tree = new KDTree();
        tree.kdtree = new Node();
        tree.buildDetail(tree.kdtree, data, m);

        return tree;
    }

    /**
     * ѭ��������
     * 
     * @param node       �ڵ�
     * @param data       ����
     * @param dimentions ���ݵ�ά��
     */
    private void buildDetail(Node node, ArrayList<double[]> data, int dimentions) {
        if (data.size() == 1) {
            node.isLeaf = true;
            node.value = data.get(0);
            return;
        }

        // ѡ�񷽲�����ά��
        node.partitionDimention = -1;
        double var = -1;
        double tmpvar;
        for (int i = 0; i < dimentions; i++) {
            tmpvar = UtilZ.variance(data, i);
            if (tmpvar > var) {
                var = tmpvar;
                node.partitionDimention = i;
            }
        }
        // �������=0����ʾ�������ݶ���ͬ���ж�ΪҶ�ӽڵ�
        if (var == 0) {
            node.isLeaf = true;
            node.value = data.get(0);
            return;
        }

        // ѡ��ָ��ֵ
        node.partitionValue = UtilZ.median(data, node.partitionDimention);

        double[][] maxmin = UtilZ.maxmin(data, dimentions);
        node.min = maxmin[0];
        node.max = maxmin[1];

        int size = (int) (data.size() * 0.55);
        ArrayList<double[]> left = new ArrayList<double[]>(size);
        ArrayList<double[]> right = new ArrayList<double[]>(size);

        for (double[] d : data) {
            if (d[node.partitionDimention] < node.partitionValue) {
                left.add(d);
            } else {
                right.add(d);
            }
        }
        Node leftnode = new Node();
        Node rightnode = new Node();
        node.left = leftnode;
        node.right = rightnode;
        buildDetail(leftnode, left, dimentions);
        buildDetail(rightnode, right, dimentions);
    }

    /**
     * ��ӡ��������ʱ��
     */
    public void print() {
        printRec(kdtree, 0);
    }

    private void printRec(Node node, int lv) {
        if (!node.isLeaf) {
            for (int i = 0; i < lv; i++)
                System.out.print("--");
            System.out.println(node.partitionDimention + ":" + node.partitionValue);
            printRec(node.left, lv + 1);
            printRec(node.right, lv + 1);
        } else {
            for (int i = 0; i < lv; i++)
                System.out.print("--");
            StringBuilder s = new StringBuilder();
            s.append('(');
            for (int i = 0; i < node.value.length - 1; i++) {
                s.append(node.value[i]).append(',');
            }
            s.append(node.value[node.value.length - 1]).append(')');
            System.out.println(s);
        }
    }

    public double[] query(double[] input) {
        Node node = kdtree;
        Stack<Node> stack = new Stack<Node>();
        while (!node.isLeaf) {
            if (input[node.partitionDimention] < node.partitionValue) {
                stack.add(node.right);
                node = node.left;
            } else {
                stack.push(node.left);
                node = node.right;
            }
        }
        /**
         * ���Ȱ���һ·�������õ�һ����ԽϽ��ľ��룬���ұ������������ĵ�
         */
        double distance = UtilZ.distance(input, node.value);
        double[] linerFindNN = queryRec(input, distance, stack);
        return linerFindNN == null ? node.value : linerFindNN;
    }

    public double[] queryRec(double[] input, double distance, Stack<Node> stack) {
        double[] linerFindNN = null;
        Node node = null;
        double tdis;
        while (stack.size() != 0) {
            node = stack.pop();
            if (node.isLeaf) {
                tdis = UtilZ.distance(input, node.value);
                if (tdis < distance) {
                    distance = tdis;
                    linerFindNN = node.value;
                }
            } else {
                /*
                 * �õ��ýڵ����ĳ������е㵽���ҵ����С����mindistance
                 * ���mindistance<distance��ʾ�п���������ڵ���ӽڵ����ҵ������ĵ�
                 * ���򲻿����ҵ�
                 */
                double mindistance = UtilZ.mindistance(input, node.max, node.min);
                if (mindistance < distance) {
                    while (!node.isLeaf) {
                        if (input[node.partitionDimention] < node.partitionValue) {
                            stack.add(node.right);
                            node = node.left;
                        } else {
                            stack.push(node.left);
                            node = node.right;
                        }
                    }
                    tdis = UtilZ.distance(input, node.value);
                    if (tdis < distance) {
                        distance = tdis;
                        linerFindNN = node.value;
                    }
                }
            }
        }
        return linerFindNN;
    }

    /**
     * ���Բ��ң����ں�kdtree��ѯ������
     * 1.�ж�kdtreeʵ���Ƿ���ȷ
     * 2.�Ƚ�����
     * 
     * @param input
     * @param data
     * @return
     */
    public static double[] linerFindNN(double[] input, double[][] data) {
        double[] linerFindNN = null;
        double dis = Double.MAX_VALUE;
        double tdis;
        for (int i = 0; i < data.length; i++) {
            tdis = UtilZ.distance(input, data[i]);
            if (tdis < dis) {
                dis = tdis;
                linerFindNN = data[i];
            }
        }
        return linerFindNN;
    }

    /**
     * ����100000�Σ������н���Ƿ�����Բ�����ͬ
     */
    public static void correct() {
        int count = 100000;
        while (count-- > 0) {
            int num = 100;
            double[][] input = new double[num][3];
            for (int i = 0; i < num; i++) {
                input[i][0] = Math.random() * 10;
                input[i][1] = Math.random() * 10;
            }
            double[] query = new double[] { Math.random() * 50, Math.random() * 50, Math.random() * 50 };

            KDTree tree = KDTree.build(input);
            double[] result = tree.query(query);
            double[] result1 = linerFindNN(query, input);
            if (result[0] != result1[0] || result[1] != result1[1]) {
                System.out.println("wrong");
                break;
            }
        }
    }

    public static void performance(int iteration, int datasize) {

        // ����database record
        int num = datasize;
        double[][] input = new double[num][2];
        for (int i = 0; i < num; i++) {
            input[i][0] = Math.random() * num;
            input[i][1] = Math.random() * num;
        }
        // ����kdtree����
        KDTree tree = KDTree.build(input);

        // ����query
        double[][] query = new double[iteration][2];
        for (int i = 0; i < iteration; i++) {
            query[i][0] = Math.random() * num * 1.5;
            query[i][1] = Math.random() * num * 1.5;
        }
        // kdtree ����
        long start = System.currentTimeMillis();
        for (int i = 0; i < iteration; i++) {
            double[] result = tree.query(query[i]);
        }
        long timekdtree = System.currentTimeMillis() - start;
        // ��������
        start = System.currentTimeMillis();
        for (int i = 0; i < iteration; i++) {
            double[] result = linerFindNN(query[i], input);
        }
        long timelinear = System.currentTimeMillis() - start;

        System.out.println("datasize:" + datasize + ";iteration:" + iteration);
        System.out.println("kdtree time cost:" + timekdtree);
        System.out.println("linear time cost:" + timelinear);
        System.out.println("linear time cost /kdtree time cost:" + (timelinear * 1.0 / timekdtree));
    }

    public static void main(String[] args) {
        correct();
        performance(10000, 1000);
    }
}