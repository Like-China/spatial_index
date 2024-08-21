package point_octree;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class OctreeTest {

    public static void testOctree() {

        // generate test data
        int n = 5000000;
        int expNB = 1000;
        int[][] inputPoint = new int[n][3];
        int[][] query3DBox = new int[n][6];
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            inputPoint[i] = new int[] { r.nextInt(50000), r.nextInt(50000), r.nextInt(50000) };
        }
        for (int i = 0; i < n; i++) {
            int x1 = r.nextInt(50000);
            int y1 = r.nextInt(50000);
            int z1 = r.nextInt(50000);
            query3DBox[i] = new int[] { x1, y1, z1, x1 + r.nextInt(10), y1 + r.nextInt(10), z1 + r.nextInt(10) };
        }
        List<Octree<Integer>.CoordHolder> res;
        long t1, t2;
        /*
         * Create a tree with a dynamic leaf size that reflects the square root of the
         * size of the growing tree.
         */
        t1 = System.currentTimeMillis();
        Octree<Integer> qDynamic = new Octree<>();
        qDynamic.DYNAMIC_MAX_OBJECTS = true;
        qDynamic.MAX_OBJ_TARGET_EXPONENT = 0.5;
        for (int i = 0; i < n; i++) {
            qDynamic.place(inputPoint[i][0], inputPoint[i][1], inputPoint[i][2], i / 3);
        }
        for (int i = 0; i < expNB; i++) {
            res = qDynamic.findAll(query3DBox[i][0], query3DBox[i][1], query3DBox[i][2],
                    query3DBox[i][3], query3DBox[i][4], query3DBox[i][5]);
            // System.out.println(res.size());
        }
        t2 = System.currentTimeMillis();
        System.out.println("Dynamic Index Time consume:" + (t2 - t1)/expNB);

        // bruteful
        for (int i = 0; i < expNB; i++) {
            int[] query = query3DBox[i];
            int count = 0;
            for (int j = 0; j < n; j++) {
                int[] db = inputPoint[j];
                if (db[0] >= query[0] && db[0] <= query[3] && db[1] >= query[1] && db[1] <= query[4]
                        && db[2] >= query[2] && db[2] <= query[5]) {
                    count++;
                }
            }
            // System.out.println(count);
        }
        t2 = System.currentTimeMillis();
        System.out.println("Bruteful Time consume:" + (t2 - t1)/expNB);
    }

    public static void main(String[] args) {
        testOctree();
    }
}
