/*
 * @Descripttion: Rika's code
 * @version: 1.0.0
 * @Author: Rika
 * @Date: 2023-11-19 13:04:00
 * @LastEditors: Rika
 * @LastEditTime: 2024-03-12 14:03:19
 */
package quadtree.rectangle_quadtree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javafx.scene.shape.Rectangle;

public class TestGenerator {
    public static void test() {
        int n = 100000;
        int totalRange = (int) Math.pow(2, 12);
        int widthRange = totalRange / 400;
        ArrayList<Rectangle> input = new ArrayList<>();
        Random r = new Random(1);

        for (int i = 0; i < n; i++) {
            int x = r.nextInt(totalRange);
            int y = r.nextInt(totalRange);
            int width = r.nextInt(widthRange) + 1;
            int height = r.nextInt(widthRange) + 1;
            while (x + width >= totalRange && y + height >= totalRange) {
                x = r.nextInt(totalRange);
                y = r.nextInt(totalRange);
                width = r.nextInt(widthRange) + 1;
                height = r.nextInt(widthRange) + 1;
            }
            input.add(new Rectangle(x, y, width, height));
        }

        int expNB = 1000;
        long t1, t2;
        t1 = System.currentTimeMillis();
        MyQuadTree quad = new MyQuadTree(0, new Rectangle(0, 0, totalRange, totalRange));
        quad.clear();
        for (Rectangle rec : input) {
            quad.insert(rec);
        }
        // retrieve
        HashSet<Rectangle> returnObjects = new HashSet<>();

        ArrayList<Integer> indexSeq = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < expNB; i++) {
            returnObjects.clear();
            indexSeq.clear();
            quad.retrieve(returnObjects, input.get(i));
            for (Rectangle dbRectangle : returnObjects) {
                if (input.get(i).intersects(dbRectangle.getBoundsInLocal())) {
                    count++;
                }
            }

        }
        System.out.println(count);
        t2 = System.currentTimeMillis();
        System.out.println("time consume:" + (t2 - t1));

        t1 = System.currentTimeMillis();
        // bruteful
        count = 0;
        for (int i = 0; i < expNB; i++) {
            for (Rectangle dbRectangle : input) {
                if (input.get(i).intersects(dbRectangle.getBoundsInLocal())) {
                    count++;
                }
            }
        }
        System.out.println(count);
        t2 = System.currentTimeMillis();
        System.out.println("time consume:" + (t2 - t1));

    }

    public static void main(String[] args) {
        test();
    }
}
