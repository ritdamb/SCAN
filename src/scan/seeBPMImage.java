package scan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class seeBPMImage {


	public static void main(String[] args) throws IOException {

		//BufferedImage image = ImageIO.read(new File("lena_gray.bmp"));
		BufferedImage image = ImageIO.read(new File("lena512.bmp"));

		int[][] array2D = new int[image.getWidth()][image.getHeight()];
		int x = 0,y = 0;
		for (int yPixel = 0; yPixel < image.getWidth(); yPixel++, x++){
			y=0;
			for (int xPixel = 0; xPixel < image.getHeight(); xPixel++, y++) {      

				int color = image.getRGB(xPixel, yPixel);
				//System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );	
				array2D[x][y] = color & 0xFF;

			}
		}
		
		getBlocks(array2D, 8);

		/*BufferedImage image2;
        for(int i=0; i<array2D.length; i++) {
            for(int j=0; j<array2D.length; j++) {
                int a = array2D[i][j];
                Color newColor = new Color(a,a,a);
                image.setRGB(j,i,newColor.getRGB());
            }
        }
        File output = new File("GrayScale.jpg");
        ImageIO.write(image, "jpg", output);*/
	}



	//La matrice in input deve essere già quadrata multiplo di N, eventualmente il controllo sulle dimensioni lo faremo prima
	public static void getBlocks (int matrix[][], int N){
		int len = matrix.length;
		ArrayList<Block> blocks = new ArrayList<Block>();

		//# di blocchi che entrano in larghezza e/o in altezza (dato che la matrice è quadrata)
		int numOfBlockPerLine = len/N;
		for(int i=1; i <= numOfBlockPerLine; i++){
			if(i%2 != 0){ //scorro da destra a sinistra
				for(int j=1; j<= numOfBlockPerLine; j++){
					int xStart = (len-1)-((N*j)-1);
					int xEnd = (len-1)-(N*(j-1));
					int yStart = (N*(i-1));
					int yEnd = (N*i)-1;

					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
			else{ //else scorro da sinistra a destra
				for(int j=numOfBlockPerLine; j>=1; j--){
					int xStart = (len-1)-((N*j)-1);
					int xEnd = (len-1)-(N*(j-1));
					int yStart = (N*(i-1));
					int yEnd = (N*i)-1;

					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
		}
		
		for(int i=0; i<blocks.size(); i++){
			Block b = blocks.get(i);
			System.out.println("Blocco: xStart="+b.xStart+"; xEnd="+b.xEnd+"; yStart="+b.yStart+"; yEnd="+b.yEnd);
		}

	}
}


