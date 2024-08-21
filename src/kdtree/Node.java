package kdtree;

public class Node {
    // 分割的维度
    public int partitionDimention;
    // 分割的值
    public double partitionValue;
    // 如果为非叶子节点，该属性为空
    // 否则为数据
    public double[] value;
    // 是否为叶子
    boolean isLeaf = false;
    // 左树
    Node left;
    // 右树
    Node right;
    // 每个维度的最小值
    double[] min;
    // 每个维度的最大值
    double[] max;
}
