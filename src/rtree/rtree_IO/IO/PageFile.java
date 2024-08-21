package rtree.rtree_IO.IO;

import org.apache.log4j.Logger;

import rtree.rtree_IO.struct.RTNode;
import rtree.rtree_IO.struct.RTree;
import rtree.rtree_IO.utils.PageFaultError;

/**
 * 页面文件用于存储各个节点信息，实现IO交互
 * 每个结点必须存储在唯一的page，根节点总是存储在page 0.
 */
public abstract class PageFile {

	/**
	 * 一个结点能拥有的最多条目个数，即分支因子
	 * branchingFactor的大小与disk block或file system page的大小一致
	 */
	public int branchingFactor;
	// 用于记录日志的Logger对象
	public static Logger logger = Logger.getLogger(PageFile.class);
	// 关联的RTree对象
	public RTree tree = null;

	/**
	 * 维度
	 */
	public int dimension = -1;

	/**
	 * 结点填充因子，0-0.5
	 * R树（R-tree）中的一个概念，用于表示节点中实际存储的条目占节点容量的比例
	 */
	public float fillFactor = -1;

	/**
	 * 结点容量
	 */
	public int nodeCapacity = -1;

	/**
	 * 一个结点以字节来存储，计算公式如下： [nodeCapacity * (sizeof(Rectangle) + sizeof(Br
	 * nch))]
	 * + parent + level + usedSpace = {nodeCapacity * [(2 * dimension *
	 * sizeof(float)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int)
	 * 
	 */
	public int pageSize = -1;

	/**
	 * 树类型
	 */
	public int treeType = -1;

	/**
	 * @param page
	 * @return 返回请求page中存储的node
	 * @throws PageFaultError
	 */
	public abstract RTNode readNode(int page) throws PageFaultError;

	/**
	 * @param node
	 * @return 将node写入第一个可用的page中，并返回此page
	 * @throws PageFaultError
	 */
	public abstract int writeNode(RTNode node) throws PageFaultError;

	/**
	 * @param page
	 * @return 标记指定的page为空
	 * @throws PageFaultError
	 */
	public abstract RTNode deletePage(int page) throws PageFaultError;

	/**
	 * PageFile初始化，为其中的属性赋值
	 * 
	 * @param tree
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param treeType
	 */
	public void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType) {
		logger.info("initializing...");
		this.dimension = dimension;
		this.fillFactor = fillFactor;
		this.nodeCapacity = capacity;
		this.treeType = treeType;
		this.tree = tree;
		// 对页面文件中节点所需的总大小的近似值
		this.pageSize = capacity * (8 * dimension + 4) + 12;
	}

	public void finalize() throws Throwable {
		super.finalize();
	}
}
