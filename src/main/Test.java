package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import io.nayuki.bmpio.BmpImage;
import io.nayuki.bmpio.BmpReader;
import io.nayuki.bmpio.Rgb888Image;

public class Test {

	public static void main(String[] args) throws IOException {
		InputStream in = new FileInputStream("22.bmp");
		BmpImage bmp;
		try {
			bmp = BmpReader.read(in);
		} finally {
			in.close();
		}
		Rgb888Image image = bmp.image;
		
	    for (int xPixel = 0; xPixel < image.getWidth(); xPixel++){
	    	for (int yPixel = 0; yPixel < image.getHeight(); yPixel++){
	            	int p = (image.getRgb888Pixel(xPixel, yPixel));
	            	System.out.println(new Color(p));
	            	System.out.println(p);
	            		
	    	}
	    }
	
	
	}

}
