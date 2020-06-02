package main.pers.xyd.bean;

import main.pers.xyd.utils.Utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class Player extends Fish implements KeyListener {
    protected int yRange;
    protected int speed;
    public int point = 0;    //分数
    public int life = 3;    //生命

    private boolean isKeydown_up = false;
    private boolean isKeydown_down = false;
    private boolean isKeydown_left = false;
    private boolean isKeydown_right = false;

    /**
     * 缺省参数的构造函数
     *
     * @param img
     * @param xRange
     * @param yRange
     */
    public Player(BufferedImage img, int xRange, int yRange) {
        this(img, 0.06f, xRange / 2, yRange / 2, -4, xRange, yRange);
    }

    /**
     * 参数最全的构造函数
     *
     * @param img    朝左的主角img
     * @param scale  缩放比例
     * @param xPos   初始位置(x)
     * @param yPos   初始位置(y)
     * @param speed  移动速度
     * @param xRange 移动范围(场景宽度)
     * @param yRange 移动范围(场景高度)
     */
    public Player(BufferedImage img, float scale, int xPos, int yPos, int speed, int xRange, int yRange) {
        super(img, Utils.reverseImage(img), scale, xPos, yPos, speed, xRange);
        this.yRange = yRange;
        this.speed = Math.abs(speed);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W)
            isKeydown_up = true;
        else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)
            isKeydown_down = true;
        else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A)
            isKeydown_left = true;
        else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D)
            isKeydown_right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W)
            isKeydown_up = false;
        else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)
            isKeydown_down = false;
        else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A)
            isKeydown_left = false;
        else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D)
            isKeydown_right = false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isKeydown_up && yPos > 10)
                yPos -= speed;
            if (isKeydown_down && yPos < yRange - getRealHeight() - 10)
                yPos += speed;
            if (isKeydown_left && xPos > 10) {
                xPos -= speed;
                dx = -speed;    //用于转向
            }
            if (isKeydown_right && xPos < xRange - getRealWidth() - 10) {
                xPos += speed;
                dx = speed;
            }
        }
    }

    public void grow(int weight) {
        scale = (float) Math.sqrt(scale * scale + (float) weight / (getImg().getWidth(null) * getImg().getHeight(null)));
    }
}
