package main.pers.xyd.applet;

import main.pers.xyd.bean.Bomb;
import main.pers.xyd.bean.Fish;
import main.pers.xyd.bean.FishFactory;
import main.pers.xyd.bean.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main extends Applet implements Runnable {
    //游戏状态
    private enum Status {
        START, PLAY, OVER
    }

    private Status status = Status.START;
    //图片与音频资源
    private Image img_background = null;
    private Image img_markBoard = null;
    private Image img_btnStart = null;
    private Image img_btnRestart = null;
    private List<BufferedImage> img_fishes = new ArrayList<>();
    private BufferedImage img_player = null;
    private Image img_bomb = null;
    private AudioClip audio_bgm = null;
    private AudioClip audio_eat = null;
    private AudioClip audio_lost = null;
    private AudioClip audio_failure = null;
    //线程相关
    private Thread judgeThread = null;
    private boolean judgeThreadAlive = false;
    //其他
    private Player player = null;//玩家
    private Bomb bomb = null;   //炸弹
    private int width, height;   //场景的宽与高

    @Override
    public void init() {
        try {
            loadResources();
        } catch (IOException e) {
            e.printStackTrace();
        }
        width = img_background.getWidth(null);
        height = img_background.getHeight(null);
        setSize(width, height);
        setLayout(null);
        audio_bgm.loop();
        //初始化造鱼工厂
        for (BufferedImage fishImg : img_fishes)
            new FishFactory(fishImg, 1, 6, width, height);
    }

    @Override
    public void start() {
        JButton btnStart = new JButton(new ImageIcon(img_btnStart));
        btnStart.addActionListener((e) -> {
            gameStart();
            remove(btnStart);
        });
        btnStart.setBounds(width / 2 - 103, height / 2 - 35, 206, 70);
        add(btnStart);

        //启动画图线程
        new Thread(this).start();
    }

    private void gameStart() {
        status = Status.PLAY;
        //初始化并启动玩家主角(小丑鱼)线程
        player = new Player(img_player, width, height);
        addKeyListener(player);
        player.start();
        //启动造鱼线程
        FishFactory.createFishThreadStart();
        //初始化并启动炸弹线程
        bomb = new Bomb(img_bomb, width, height);
        bomb.start();
        //启动判分线程
        judgeThreadStart(player, FishFactory.fishList);
    }

    private void gameOver() {
        FishFactory.createFishThreadStop(); //关闭工厂造鱼线程
        player.stop();                      //关闭玩家的线程
        removeKeyListener(player);          //停止接受键盘事件
        bomb.threadStop = true;     //停止炸弹的线程
        bomb = null;
        status = Status.OVER;   //设置游戏状态
        //创建重新开始的按钮
        JButton btnRestart = new JButton(new ImageIcon(img_btnRestart));
        btnRestart.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStart();
                remove(btnRestart);
            }
        });
        int btnRestartWidth = img_btnRestart.getWidth(null);
        int btnRestartHeight = img_btnRestart.getHeight(null);
        btnRestart.setBounds((width - btnRestartWidth) / 2, 495, btnRestartWidth, btnRestartHeight);
        add(btnRestart);
    }

    @Override
    public void paint(Graphics g) {
        switch (status) {
            case START:
                paint_start(g);
                break;
            case PLAY:
                paint_play(g);
                break;
            case OVER:
                paint_over(g);
                break;
        }
    }

    private void paint_start(Graphics g) {
        //画背景
        g.drawImage(img_background, 0, 0, null);
        //画开始控件
        paintComponents(g);
    }

    private void paint_over(Graphics g) {
        //画背景
        g.drawImage(img_background, 0, 0, null);
        //画分数界面
        int x = (width - img_markBoard.getWidth(null)) / 2;
        int y = (height - img_markBoard.getHeight(null)) / 2;
        g.drawImage(img_markBoard, x, y, null);
        g.setColor(Color.blue);
        g.setFont(new Font("HGHP_CNKI", Font.ITALIC, 60));
        g.drawString(player.point + "", x + 260, y + 320);
        paintComponents(g);
    }

    private void paint_play(Graphics g) {
        //画背景
        g.drawImage(img_background, 0, 0, null);
        //画各种各样的鱼
        synchronized (FishFactory.fishList) {
            for (Fish fish : FishFactory.fishList)
                g.drawImage(fish.getImg(), fish.getxPos(), fish.getyPos(), fish.getRealWidth(), fish.getRealHeight(), null);
        }
        //画炸弹
        if (bomb != null)
            g.drawImage(bomb.getImg(), bomb.getxPos(), bomb.getyPos(), null);
        //画玩家及生命值、分数
        if (player != null)
            synchronized (player) {
                g.drawImage(player.getImg(), player.getxPos(), player.getyPos(), player.getRealWidth(), player.getRealHeight(), null);
                //画生命值
                int posX = 10;
                int posY = 5;
                int lifeWidth = (int) (width * 0.08);
                int lifeHeight = (int) (height * 0.08);
                for (int life = 0; life < player.life; life++) {
                    g.drawImage(img_player.getScaledInstance(lifeWidth, lifeHeight, Image.SCALE_DEFAULT), posX, posY, null);
                    posX += (lifeWidth + 5);
                }
                //画分数
                posX = 10;
                posY += (lifeHeight + 40);
                g.setColor(Color.red);
                g.setFont(new Font("HGHP_CNKI", Font.ITALIC, 30));
                g.drawString("分数:" + player.point, posX, posY);
            }
    }

    @Override
    public void update(Graphics g) {
        Image bufImage = createImage(getWidth(), getHeight());
        Graphics bufG = bufImage.getGraphics();
        paint(bufG);
        g.drawImage(bufImage, 0, 0, null);
    }

    private void loadResources() throws IOException {
        URL resourceUrl = null;
        ClassLoader cl = getClass().getClassLoader();
        //加载背景资源
        resourceUrl = cl.getResource("resources/image/background.jpg");
        img_background = ImageIO.read(resourceUrl);
        //加载UI:分数面板
        resourceUrl = cl.getResource("resources/image/markBoard.png");
        img_markBoard = ImageIO.read(resourceUrl);
        //加载UI:开始按钮
        resourceUrl = cl.getResource("resources/image/btnStart.png");
        img_btnStart = ImageIO.read(resourceUrl);
        //加载UI:重新开始按钮
        resourceUrl = cl.getResource("resources/image/btnRestart.png");
        img_btnRestart = ImageIO.read(resourceUrl);
        //加载鱼的资源
        for (int i = 0; i < 19; i++) {
            resourceUrl = cl.getResource("resources/image/fish" + i + ".png");
            img_fishes.add(ImageIO.read(resourceUrl));
        }
        //加载主角(玩家)的资源
        resourceUrl = cl.getResource("resources/image/player.png");
        img_player = ImageIO.read(resourceUrl);
        //加载炸弹资源
        resourceUrl = cl.getResource("resources/image/bomb.png");
        img_bomb = ImageIO.read(resourceUrl);
        //加载音效资源
        resourceUrl = cl.getResource("resources/audio/bgm.wav");
        audio_bgm = Applet.newAudioClip(resourceUrl);
        resourceUrl = cl.getResource("resources/audio/eat.wav");
        audio_eat = Applet.newAudioClip(resourceUrl);
        resourceUrl = cl.getResource("resources/audio/lost.wav");
        audio_lost = Applet.newAudioClip(resourceUrl);
        resourceUrl = cl.getResource("resources/audio/failure.wav");
        audio_failure = Applet.newAudioClip(resourceUrl);
    }

    /**
     * 重绘刷新界面线程
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repaint();
        }
    }

    private void judgeThreadStart(Player player, List<Fish> fishList) {
        if (judgeThread != null && judgeThread.isAlive())
            judgeThread.stop();
        judgeThreadAlive = true;
        (judgeThread = new Thread(() -> {
            while (judgeThreadAlive) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Fish fish : fishList)
                    if (player.contactWith(fish)) {
                        if (player.getWeight() > fish.getWeight()) {
                            audio_eat.play();
                            FishFactory.removeFish(fish);   //吃掉小鱼
                            player.grow(100);       //增长体重
                            player.point += 15;             //得分
                        } else {
                            //失去一条生命
                            lostAlive();
                        }
                    }
                if (bomb != null && bomb.contactWith(player)) {
                    bomb.resetPos();
                    lostAlive();

                }
            }
        })).start();
    }

    private void lostAlive() {
        audio_lost.play();
        if (--player.life <= 0) {
            audio_failure.play();
            gameOver();
            return; //退出线程
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

