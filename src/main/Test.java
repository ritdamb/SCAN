package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import scan.Block;
import scan.seeBPMImage;


public class Test {

	public static void main(String[] args) throws IOException {
		
		
		ArrayList<Path> scan4 = new ArrayList<Path>();
		populate(scan4);
		//scan path 4x4
		for (Path path : scan4) {
			//System.out.println(path);
		}
		
		//conversione scanpath 8x8
		
		ArrayList<Path> scan8 = new ArrayList<Path>();

		
		for (Path p : scan4) {
			scan8.add(new Path(p.getX(),p.getY()+4));
		}
		
		int i=15;
		int n=8;
		int goRx=0,goUp=0,goDown=0,goLx=0;
		
		/**
		 * 02 
		 * 12
		 * 22
		 * 32
		 * 42
		 * 52
		 * 
		 */
		
		while( i < ((n*n)-1) ){
			Path currentPath = scan8.get(i);
			if(currentPath.getY() == n-1){ // cambio riga
				scan8.add(new Path(currentPath.getX()+1, currentPath.getY()));
				scan8.add(new Path(currentPath.getX()+1, currentPath.getY()-1));
				goLx=currentPath.getX();
				goUp = currentPath.getX()+1;
				goDown=currentPath.getX()+1;
				goRx=currentPath.getX()+2;
				i= i+2;
			}else if( goLx != 0 ){ // vado a sinistra
				scan8.add(new Path(currentPath.getX(), currentPath.getY()-1));
				i++;
				goLx--;
				
			}else if(goUp != 0){ // salgo
				scan8.add(new Path(currentPath.getX()-1, currentPath.getY()));
				i++;
				goUp--;
			}else if(goUp == 0 && currentPath.getX() == 0){//cambio colonna
				scan8.add(new Path(currentPath.getX(), currentPath.getY()-1));
				scan8.add(new Path(currentPath.getX()+1, currentPath.getY()-1));
				i= i+2;
			}else if(goDown != 0 ){// scendo
				scan8.add(new Path(currentPath.getX()+1, currentPath.getY()));
				i++;
				goDown--;
			}else if(goRx != 0){ // vado a destra
				scan8.add(new Path(currentPath.getX(), currentPath.getY()+1));
				i++;
				goRx--;
			}
		}
		
		/*for(Path p : scan8)
			System.out.println(p);
		*/
		int[][] matrix = getMatrix();
		ArrayList<Block> blocks = seeBPMImage.getBlocks(matrix, 8);

		
		scanOnImage(scan8,blocks.get(0),matrix);
		
		
	}
	
	private static void scanOnImage(ArrayList<Path> scan8, Block block, int[][] matrix) {
		int i=0;
		int x1=0,y1=0,posx=0,posy=0,x2=0,y2=0;
		Path p = scan8.get(i); 
		
		//non controllo che parto da noth-east poi va inserito
		int pixel = matrix[block.getxEnd()][block.getyStart()];
		System.out.println("["+ block.getxEnd()+"]["+block.getyStart() +"]:" + pixel);
		x1=p.getX();
		y1=p.getY();
		posx=block.getxEnd();
		posy=block.getyStart();
		
		for(i=1; i < scan8.size(); i++){
			p = scan8.get(i);
			x2=p.getX();
			y2=p.getY();
			
			int x=posx +(y2-y1);
			int y=posy+(x2-x1);
			
			pixel= matrix[x][y];
			System.out.println("["+ x+"]["+y+"]:" + pixel);
			
			posx=x;
			posy=y;
			x1=x2;
			y1=y2;
			
			
		}
		
	}

	public static int[][] getMatrix() throws IOException{
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
		
		return array2D;
	}
	
	public static void populate(ArrayList<Path> scan){
		String file="";
		try {
			file = new String(Files.readAllBytes(Paths.get("scanpath.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(file.length()!= 0){
			char xy[] = new char[2];
			file.getChars(0, 2, xy, 0);
			file = file.substring(2);
			scan.add(new Path( Integer.parseInt(""+xy[0]), Integer.parseInt(""+xy[1])));
		}

	}

}
