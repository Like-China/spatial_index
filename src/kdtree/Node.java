package kdtree;

public class Node {
    // �ָ��ά��
    public int partitionDimention;
    // �ָ��ֵ
    public double partitionValue;
    // ���Ϊ��Ҷ�ӽڵ㣬������Ϊ��
    // ����Ϊ����
    public double[] value;
    // �Ƿ�ΪҶ��
    boolean isLeaf = false;
    // ����
    Node left;
    // ����
    Node right;
    // ÿ��ά�ȵ���Сֵ
    double[] min;
    // ÿ��ά�ȵ����ֵ
    double[] max;
}
