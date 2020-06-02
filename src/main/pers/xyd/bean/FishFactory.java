package main.pers.xyd.bean;

import main.pers.xyd.utils.Utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 该类用于生成Fish实例，根据不同的配置生成不同的实例
 */
public class FishFactory {
    public static List<FishFactory> factories = new ArrayList<>();
    public static CopyOnWriteArrayList<Fish> fishList = new CopyOnWriteArrayList<>();

    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1));
    public static Thread createFishThread = null;
    public static boolean createFishThreadAlive = false;

    private BufferedImage img_left;
    private BufferedImage img_right;
    private int speed_min;
    private int speed_max;
    private float scale_min = 0.2f;    //鱼的缩放比例(最小)
    private float scale_max = 2f;    //鱼的缩放比例(最大)
    private int xRange;         //x方向活动范围(场景宽度)
    private int yRange;         //y方向活动范围(场景高度)

    /**
     * 启动或重启一个造鱼线程
     */
    public static void createFishThreadStart() {
        if (createFishThread != null && createFishThread.isAlive())
            createFishThread.stop();
        createFishThreadAlive = true;
        (createFishThread = new Thread(() -> {
            while (createFishThreadAlive) {
                try {
                    Thread.sleep(500);     //生成鱼的周期
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (factories.isEmpty() || executor.getActiveCount() >= 20) //最多不超过20条鱼
                    continue;
                if (executor.getActiveCount() < 10 || new Random().nextBoolean())   //小于10条鱼必定生成，否则一半概率生成
                    factories.get(new Random().nextInt(factories.size())).creatFish();  // 调用随机一个工厂的造鱼方法
            }
        })).start();
    }

    /**
     * 关闭造鱼线程，同时清空所有的鱼(及其线程)
     */
    public static void createFishThreadStop() {
        createFishThreadAlive = false;
        executor.purge();   //关闭所有鱼的线程
        removeAll();        //移除所有鱼
    }

    /**
     * 构造一个鱼的工厂(模板)
     *
     * @param img       鱼的img图像，朝左
     * @param speed_min 鱼的最小速度
     * @param speed_max 鱼的最大速度
     * @param xRange    鱼的x方向活动范围(场景宽度)
     * @param yRange    鱼的y方向活动范围(场景高度)
     */
    public FishFactory(BufferedImage img, int speed_min, int speed_max, int xRange, int yRange) {
        this.img_left = img;
        img_right = Utils.reverseImage(img);
        this.speed_min = speed_min;
        this.speed_max = speed_max;
        this.xRange = xRange;
        this.yRange = yRange;
        factories.add(this);
    }

    public synchronized void creatFish() {
        Random random = new Random();
        int xPos, yPos, dx;
        float scale = random.nextFloat() * (scale_max - scale_min) + scale_min;
        dx = random.nextInt(speed_max - speed_min) + speed_min;
        yPos = random.nextInt(yRange);
        if (random.nextBoolean()) {
            //朝左的鱼
            xPos = xRange;
            dx = -dx;
        } else {
            //朝右的鱼
            xPos = -(int) (img_left.getWidth() * scale);
        }
        Fish fish = new Fish(img_left, img_right, scale, xPos, yPos, dx, xRange);
        fishList.add(fish);
        executor.execute(fish);
    }

    public static void removeFish(Fish fish) {
        fish.threadStop = true;
        fishList.remove(fish);      //将鱼从列表中移除
    }

    public static void removeAll() {
        for (Fish fish : fishList)
            removeFish(fish);
    }
}
