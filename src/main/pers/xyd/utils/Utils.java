package main.pers.xyd.utils;

import java.awt.image.BufferedImage;

public class Utils {
    /**
     * 将BufferedImage水平翻转
     * @param src
     * @return
     */
    public static BufferedImage reverseImage(BufferedImage src) {
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        BufferedImage des = new BufferedImage(width, height, src.getType());
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                des.setRGB(x, y, src.getRGB(width - x, y));
            }
        }
        return des;
    }
}
