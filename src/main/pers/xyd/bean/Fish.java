package main.pers.xyd.bean;

import java.awt.*;
import java.util.Random;

/**
 * 实体类的鱼，一个实例就是场景中的一条鱼
 */
public class Fish extends Thread {
    protected Image img_left; //朝左的鱼
    protected Image img_right;//朝右的鱼
    protected float scale;    //放大/缩小比例
    protected int xPos;
    protected int yPos;
    protected int dx;         //x增量(x速度)
    protected int xRange;     //活动范围(场景宽度)
    public boolean threadStop = false;  //控制线程的关闭

    /**
     * 获取当前朝向的img
     *
     * @return
     */
    public Image getImg() {
        if (dx > 0)
            return img_right;
        else
            return img_left;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    /**
     * 获取实例的宽度(图像宽度*比例)
     *
     * @return
     */
    public int getRealWidth() {
        return (int) (img_left.getWidth(null) * scale);
    }

    public int getRealHeight() {
        return (int) (img_left.getHeight(null) * scale);
    }

    /**
     * 获取鱼的大小(显示的面积)
     *
     * @return
     */
    public int getWeight() {
        return getRealWidth() * getRealHeight();
    }

    public Fish(Image img_left, Image img_right, float scale, int xPos, int yPos, int dx, int xRange) {
        this.img_left = img_left;
        this.img_right = img_right;
        this.scale = scale;
        this.xPos = xPos;
        this.yPos = yPos;
        this.dx = dx;
        this.xRange = xRange;
    }

    @Override
    public synchronized void run() {
        while (!threadStop) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            xPos += dx;
            if (xPos < -getRealWidth() || xPos > xRange) {
                FishFactory.removeFish(this);
                break;
            }
            if (new Random().nextInt(1000) < 1)
                dx = -dx;
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
        Fish fish_L_U, fish_R_D;
        if (this.getyPos() > fish.getyPos()) {
            fish_L_U = fish;
            fish_R_D = this;
        } else {
            fish_L_U = this;
            fish_R_D = fish;
        }
        if (fish_R_D.getyPos() > fish_L_U.getyPos() + fish_L_U.getRealHeight())
            return false;
        if (this.getxPos() > fish.getxPos()) {
            fish_L_U = fish;
            fish_R_D = this;
        } else {
            fish_L_U = this;
            fish_R_D = fish;
        }
        if (fish_R_D.getxPos() > fish_L_U.getxPos() + fish_L_U.getRealWidth())
            return false;
        return true;
    }
}
