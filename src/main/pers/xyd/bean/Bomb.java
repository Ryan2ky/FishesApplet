package main.pers.xyd.bean;

import java.awt.*;
import java.util.Random;

public class Bomb extends Thread {
    private Image img;
    private int xPos;
    private int yPos;
    private int dy;
    private int xRange;
    private int yRange;
    public boolean threadStop = false;

    public Bomb(Image img, int xRange, int yRange) {
        this.img = img;
        dy = 5;
        this.xRange = xRange;
        this.yRange = yRange;
        resetPos();
    }

    public Image getImg() {
        return img;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    /**
     * 重置小鱼的位置
     */
    public void resetPos() {
        Random random = new Random();
        xPos = random.nextInt(xRange);
        yPos = -img.getHeight(null);
    }

    @Override
    public void run() {
        while (!threadStop) {
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            yPos += dy;
            if (yPos > yRange) {
                resetPos();
                try {
                    Thread.sleep(new Random().nextInt(5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        threadStop = false;
    }

    /**
     * 判断是否与另外一条鱼接触
     *
     * @param fish
     * @return
     */
    public boolean contactWith(Fish fish) {
        int bx1 = this.getxPos();
        int by1 = this.getyPos();
        int bx2 = bx1 + this.img.getWidth(null);
        int by2 = by1 + this.img.getHeight(null);
        int fx1 = fish.getxPos();
        int fy1 = fish.getyPos();
        int fx2 = fx1 + fish.getRealWidth();
        int fy2 = fy1 + fish.getRealHeight();

        if ((bx1 > fx1 && bx1 > fx2) || (bx1 < fx1 && bx2 < fx1))
            return false;
        if ((by1 > fy1 && by1 > fy2) || (by1 < fy1 && by2 < fy1))
            return false;
        return true;
    }
}
