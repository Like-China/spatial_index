package rtree.rtree_IO.IO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

import rtree.rtree_IO.struct.RTDataNode;
import rtree.rtree_IO.struct.RTDirNode;
import rtree.rtree_IO.struct.RTNode;
import rtree.rtree_IO.struct.RTree;
import rtree.rtree_IO.utils.PageFaultError;
import rtree.rtree_IO.utils.Point;
import rtree.rtree_IO.utils.Rectangle;

/*
 * 实现了一个持久化的页面文件，用于在R树中存储节点
 */
public class PersistentPageFile extends PageFile {
	// 一个RandomAccessFile对象，用于在文件中进行随机访问
	private RandomAccessFile file;
	private String fileName;
	// 用于跟踪空闲页面的页号
	private Stack<Integer> emptyPages = new Stack<>();

	/**
	 * 头部大小，包括维度、填充因子、节点容量、页面大小和树类型等信息
	 * 计算公式如下： headerSize = dimension + fillFactor + nodeCapacity + pageSize +
	 * treeType
	 */
	private int headerSize = 20;
	// 一个表示空页面的常量，其值为-2
	public static final int EMPTY_PAGE = -2;

	/**
	 * 创建临时文件，退出虚拟机时删除
	 */
	public PersistentPageFile() {
		this(null);
	}

	/**
	 * @param fileName
	 */
	public PersistentPageFile(String fileName) {
		try {
			// 如果fileName为空则创建临时文件，退出虚拟机时删除
			if (fileName == null) {
				File f = File.createTempFile("rtreeTemp", ".dat");
				this.fileName = f.getCanonicalPath();
				System.out.println(this.fileName);
				f.deleteOnExit();// 在虚拟机终止时，请求删除此抽象路径名表示的文件或目录。
			} else {
				file = new RandomAccessFile(fileName, "rw");
				this.fileName = fileName;

				file.seek(0);
				byte[] header = new byte[headerSize];
				if (headerSize == file.read(header)) {// 将最多
														// header.length个数据字节从此文件读入
														// byte数组。在至少一个输入字节可用前，此方法一直阻塞。
					DataInputStream dis = new DataInputStream(
							new ByteArrayInputStream(header));
					dimension = dis.readInt();
					fillFactor = dis.readFloat();
					nodeCapacity = dis.readInt();
					pageSize = dis.readInt();
					treeType = dis.readInt();

					// 找到所有的空page,并将它们添加在emptyPages栈中
					int i = 0;
					try {
						while (true) {
							if (EMPTY_PAGE == file.readInt()) {
								emptyPages.push(i);
							}
							i++;
							file.seek(headerSize + i * pageSize);
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType) {
		super.initialize(tree, dimension, fillFactor, capacity, treeType);
		emptyPages.clear();

		try {
			file.setLength(0);

			file.seek(0);
			file.writeInt(dimension);
			file.writeFloat(fillFactor);
			file.writeInt(nodeCapacity);
			file.writeInt(pageSize);
			file.writeInt(treeType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finalize() throws Throwable {
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	@Override
	public RTNode readNode(int page) throws PageFaultError {
		if (page < 0) {
			throw new IllegalArgumentException(
					"Page number cannot be negative.");
		}

		try {
			file.seek(headerSize + page * pageSize);// 先定位到指定page

			byte[] b = new byte[pageSize];
			int l = file.read(b);
			if (-1 == l) {
				throw new PageFaultError("EOF found while trying to read page "
						+ page + ".");
			}

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					b));

			int parent = dis.readInt();
			if (parent == EMPTY_PAGE) {
				throw new PageFaultError("Page " + page + " is empty.");
			}

			int level = dis.readInt();
			int usedSpace = dis.readInt();

			RTNode node;
			if (level != 0) {
				node = new RTDirNode(tree, parent, page, level);
			} else {
				node = new RTDataNode(tree, parent, page);
			}

			// node.parent = page;//多余
			// node.level = level;//多余
			node.usedSpace = usedSpace;

			float[] p1 = new float[dimension];
			float[] p2 = new float[dimension];

			for (int i = 0; i < usedSpace; i++) {
				for (int j = 0; j < dimension; j++) {
					p1[j] = dis.readFloat();
					p2[j] = dis.readFloat();
				}

				node.datas[i] = new Rectangle(new Point(p1), new Point(p2));
				node.branches[i] = dis.readInt();
			}

			return node;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int writeNode(RTNode node) throws PageFaultError {
		if (node == null) {
			throw new IllegalArgumentException("Node cannot be null.");
		}

		try {
			int page;
			if (node.pageNumber < 0) {
				if (emptyPages.empty()) {
					page = (int) ((file.length() - headerSize) / pageSize);
				} else {
					page = emptyPages.pop();
				}
				node.pageNumber = page;
			} else {
				page = node.pageNumber;
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream(pageSize);
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(node.parent);
			dos.writeInt(node.level);
			dos.writeInt(node.usedSpace);

			for (int i = 0; i < tree.getNodeCapacity(); i++) {
				for (int j = 0; j < tree.getDimension(); j++) {
					if (node.datas[i] == null) {
						dos.writeFloat(Float.NaN);
						dos.writeFloat(Float.NaN);
					} else {
						dos.writeFloat(node.datas[i].getLow()
								.getFloatCoordinate(j));
						dos.writeFloat(node.datas[i].getHigh()
								.getFloatCoordinate(j));
					}
				}
				dos.writeInt(node.branches[i]);
			}
			dos.flush();

			file.seek(headerSize + page * pageSize);
			file.write(baos.toByteArray());

			return page;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public RTNode deletePage(int page) throws PageFaultError {
		try {
			if (page < 0 || page > (file.length() - headerSize) / pageSize) {
				return null;
			} else {
				if (page == 5) {
					System.out.println("=======5=======");
				}
				System.out.println("----delete page " + page + "-----");
				RTNode node = readNode(page);
				file.seek(headerSize + page * pageSize);
				file.writeInt(EMPTY_PAGE);
				emptyPages.push(page);
				return node;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
