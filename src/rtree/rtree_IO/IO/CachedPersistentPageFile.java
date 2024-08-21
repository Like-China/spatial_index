package rtree.rtree_IO.IO;

import java.util.HashMap;
import java.util.Map;

import rtree.rtree_IO.struct.RTNode;
import rtree.rtree_IO.utils.PageFaultError;

/*
 * 一个基本的缓存机制，以优化对持久性页文件中节点的读取和写入操作
 * 缓存策略涉及为每个缓存对象维护一个排名，当对象被读取、写入或删除时，排名会被更新。当缓存已满时，最近未使用的对象将被淘汰
 */
public class CachedPersistentPageFile extends PersistentPageFile {
	private Map<Integer, CachedObject> cache;
	private int usedSpace = 0;
	private int cacheSize = 0;

	public CachedPersistentPageFile(String fileName, int cacheSize) {
		super(fileName);
		this.cacheSize = cacheSize;
		cache = new HashMap<Integer, CachedObject>(cacheSize);
	}

	public RTNode readNode(int page) throws PageFaultError {
		RTNode n = readFromCache(page);
		if (null != n) {
			return n;
		} else {
			return super.readNode(page);
		}
	}

	public int writeNode(RTNode n) throws PageFaultError {
		int page = super.writeNode(n);
		writeToCache(n, page);
		return page;
	}

	private RTNode readFromCache(int page) {
		CachedObject c = cache.get(page);
		if (c != null) {
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
			return c.object;
		} else {
			return null;
		}
	}

	private void writeToCache(RTNode o, int page) {
		CachedObject c = cache.get(page);

		if (null != c) {
			c.object = o;
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
		} else if (usedSpace < cacheSize) {// cache is not full
			cache.put(page, new CachedObject(o, page, usedSpace));
			usedSpace++;
			return;
		} else {// cache is full
			for (CachedObject co : cache.values()) {
				if (co.rank == 0) {
					cache.remove(co.page);
					break;
				}
			}
			for (CachedObject co : cache.values()) {
				co.rank--;
			}

			cache.put(page, new CachedObject(o, page, usedSpace - 1));
		}
	}

	public RTNode deletePage(int page) throws PageFaultError {
		CachedObject c = cache.get(page);
		if (c != null) {
			int rank = c.rank;

			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				}
			}

			cache.remove(page);
			usedSpace--;
		}
		return super.deletePage(page);
	}
}

// 具有排名信息的缓存RTNode对象
class CachedObject {
	int rank = 0;
	int page = -1;
	RTNode object;

	public CachedObject(RTNode o, int page, int rank) {
		this.object = o;
		this.page = page;
		this.rank = rank;
	}
}