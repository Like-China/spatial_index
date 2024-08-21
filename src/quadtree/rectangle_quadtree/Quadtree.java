package quadtree.rectangle_quadtree;

/* Reference
* https://code.tutsplus.com/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374t
* coding：gbk
这是带测试输出和中间输出提醒的测试代码，使用包装好的Rectangle类，query和db都是Rectangle 可用于碰撞检测
在执行自己任务时，如果不使用图形界面，可以自己定义Rectangle类以添加新的属性
返回的结果只是属于同一小区域，但不不一定又交集,需要进一步验证
III  IV
II  I
原始的Quadtree 输出结果的正确性已经得到验证 但效率并不比Rec直接做intersection暴力判断好
*/
import java.util.ArrayList;
import java.util.HashSet;
import javafx.scene.shape.Rectangle;

public class Quadtree {
    // how many objects a node can hold before it splits
    public int MAX_OBJECTS = 20;
    // the deepest level subnode
    public int MAX_LEVELS = 10;
    // the current node level (0 being the topmost node)
    public int level;
    // objects that cannot completely fit within a child node and is part of the
    // parent node
    public ArrayList<Rectangle> objects = new ArrayList<>();
    // the 2D space that the node occupies Rectangle(min_x, min_y, width, height)
    public Rectangle bounds;
    // the four subnodes
    public Quadtree[] nodes;

    /*
     * Constructor
     */
    public Quadtree(int pLevel, Rectangle pBounds) {
        level = pLevel;
        objects = new ArrayList<>();
        bounds = pBounds;
        nodes = new Quadtree[4];
    }

    /*
     * Clears the quadtree by recursively clearing all objects from all nodes
     */
    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    /*
     * Splits the node into 4 subnodes
     * The split method splits the node into four subnodes by dividing the node into
     * four equal parts and initializing the four subnodes with the new bounds.
     */
    public void split() {
        int subWidth = (int) (bounds.getWidth() / 2);
        int subHeight = (int) (bounds.getHeight() / 2);
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        nodes[0] = new Quadtree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    /*
     * Determine which node the object belongs to. -1 means object cannot completely
     * fit within a child node and is part of the parent node
     */
    public int getIndex(Rectangle pRect) {
        int index = -1;
        double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
        // Object can completely fit within the top quadrants
        boolean topQuadrant = (pRect.getY() < horizontalMidpoint
                && pRect.getY() + pRect.getHeight() < horizontalMidpoint);
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (pRect.getY() > horizontalMidpoint);
        // Object can completely fit within the left quadrants
        if (pRect.getX() < verticalMidpoint && pRect.getX() + pRect.getWidth() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        }
        // Object can completely fit within the right quadrants
        else if (pRect.getX() > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }
        return index;
    }

    /*
     * Insert the object into the quadtree. If the node exceeds the capacity, it
     * will split and add all objects to their corresponding nodes.
     */
    public void insert(Rectangle pRect) {
        // nodes[0] is not null iff nodes has been split
        if (nodes[0] != null) {
            int index = getIndex(pRect);
            if (index != -1) {
                nodes[index].insert(pRect);
                return;
            }
        }
        // if object cannot completely fit within a child node and is part of the parent
        // node, store it in the parent node
        objects.add(pRect);
        // When reaching the max capacity, split and assign each recrangle into
        // corresponding subnode
        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }
            int i = 0;

            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                }
                i++;
            }
        }
    }

    /*
     * Return all objects that could collide with the given object
     * It returns all objects in all nodes that the given object could potentially
     * collide with. This method is what helps to reduce the number of pairs to
     * check collision against.
     */
    public ArrayList<Integer> retrieve(ArrayList<Integer> indexSeq,
            HashSet<Rectangle> returnObjects, Rectangle pRect) {
        int index = getIndex(pRect);
        indexSeq.add(index);
        // 检查对象是否完全适应于子节点，即区域被子节点区域覆盖
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(indexSeq, returnObjects, pRect);
        }
        // 总是将当前节点的对象添加到结果集
        returnObjects.addAll(objects);
        // 修改如下 检查对象是否与子节点重叠（i.e., -1 status)，并将它们的对象添加到结果集
        if (index == -1 && nodes[0] != null) {
            for (Quadtree node : nodes) {
                if (node.bounds.intersects(pRect.getBoundsInLocal())) {
                    node.retrieve(indexSeq, returnObjects, pRect);
                }
            }
        }
        return indexSeq;
    }

    /* Traveling the Graph using Depth First Search */
    public void dfs() {
        if (this == null || this.nodes[0] == null)
            return;
        System.out.println("\n*******Parent Node*******");
        System.out.printf("Level = %d [X1=%03.1f Y1=%03.1f] \t[X2=%03.1f Y2=%03.1f] -1 count = %d",
                this.level, this.bounds.getX(), this.bounds.getY(),
                this.bounds.getX() + this.bounds.getWidth(), this.bounds.getY() + this.bounds.getHeight(),
                this.objects.size());

        System.out.println("\nChild Node");
        for (Quadtree node : this.nodes) {
            System.out.printf("Level = %d [X1=%03.1f Y1=%03.1f] \t[X2=%03.1f Y2=%03.1f] eleCount = %d\n",
                    node.level, node.bounds.getX(), node.bounds.getY(),
                    node.bounds.getX() + node.bounds.getWidth(), node.bounds.getY() + node.bounds.getHeight(),
                    node.objects.size());
            if (node.nodes[0] == null) {
                System.out.printf("\t  Leaf Node. \n");
            }
        }

        for (int i = 0; i < 4; i++) {
            if (this.nodes[i] != null)
                this.nodes[i].dfs();
        }

    }

}
