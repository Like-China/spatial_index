package rtree.rtree_IO.struct;

import rtree.rtree_IO.utils.Rectangle;

public interface INode {
	public RTNode getParent();

	public String getUniqueId();

	public int getLevel();

	public Rectangle getNodeRectangle();

	public boolean isLeaf();

	public boolean isRoot();

	public boolean isIndex();
}
