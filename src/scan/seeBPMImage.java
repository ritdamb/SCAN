package scan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class seeBPMImage {


    public static void main(String[] args) throws IOException {

        //BufferedImage image = ImageIO.read(new File("/Users/giovanni/lena_gray.bmp"));
        BufferedImage image = ImageIO.read(new File("lena512.bmp"));
        int[][] array2D = new int[image.getWidth()][image.getHeight()];

        //byte[] pixels = extractBytes("/Users/giovanni/lena_gray.bmp");
        //byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        //System.out.println(pixels.length);
        //for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
        for (int xPixel = 0; xPixel < 100; xPixel++) {
            //for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
            for (int yPixel = 0; yPixel < 100; yPixel++) {

                    int color = image.getRGB(xPixel, yPixel);
                    System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );
                    if (color == Color.BLACK.getRGB()) {
                        array2D[xPixel][yPixel] = 1;
                    } else array2D[xPixel][yPixel] = 0; // ?
            }
        }
    }

    public static byte[] extractBytes (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return ( data.getData() );
    }
}
