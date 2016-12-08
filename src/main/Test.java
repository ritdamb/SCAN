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
	
	 static final String NORTH_EAST="NORTH_EAST";
	 static final String NORTH_OVEST="NORTH_OVEST";
	 static final String SOUTH_OVEST="SOUTH_OVEST";
	 static final String SOUTH_EAST="SOUTH_EAST";
	 static final String  O0="03021213232221110100102030313233";
	 static final String O1="00101101021222212030313233231303";
	 static final String O2="30312120101112223233231303020100";
	 static final String O3="33232232312111121303020100102030";
	 
	public static void main(String[] args) throws IOException {
		
		

		ArrayList<Path> scan4O1 = new ArrayList<Path>();
		populate(scan4O1,O1);
		
		
		int[][] matrix = getMatrix();
		ArrayList<Block> blocks = seeBPMImage.getBlocks(matrix, 8);

		//scan path north-east
		//ArrayList<Path> scan8ne = extendsO0(scan4O0);
		//scan path north-ovest
		ArrayList<Path> scan8no = extendsO1(scan4O1);
		
		scanOnImage(scan8no,blocks.get(0),matrix, NORTH_OVEST);
		
		
	}
	
	private static ArrayList<Path> extendsO1(ArrayList<Path> scan4){
		//conversione scanpath 8x8
		
				ArrayList<Path> scan8no = new ArrayList<Path>();

				
				for (Path p : scan4) {
					scan8no.add(p);
				}
				
				int i=15;
				int n=8;
				int goRx=0,goUp=0,goDown=0,goLx=0;
				
				
				while( i < ((n*n)-1) ){
					Path currentPath = scan8no.get(i);
					if(currentPath.getX() == 0){ // cambio colonna
						scan8no.add(new Path(currentPath.getX(), currentPath.getY()+1));
						scan8no.add(new Path(currentPath.getX()+1, currentPath.getY()+1));
						goLx=currentPath.getY()+1;
						goUp = currentPath.getY()+2;
						goDown=currentPath.getY();
						goRx=currentPath.getY()+1;
						i= i+2;
					}else if(goDown != 0 ){// scendo
						scan8no.add(new Path(currentPath.getX()+1, currentPath.getY()));
						i++;
						goDown--;
					}else if( goLx != 0 ){ // vado a sinistra
						scan8no.add(new Path(currentPath.getX(), currentPath.getY()-1));
						i++;
						goLx--;
						
					}else if(goLx == 0 && currentPath.getY() == 0){//cambio riga
						scan8no.add(new Path(currentPath.getX()+1, currentPath.getY()));
						scan8no.add(new Path(currentPath.getX()+1, currentPath.getY()+1));
						i= i+2;
					}else if(goRx != 0){ // vado a destra
						scan8no.add(new Path(currentPath.getX(), currentPath.getY()+1));
						i++;
						goRx--;
					}else if(goUp != 0){ // salgo
						scan8no.add(new Path(currentPath.getX()-1, currentPath.getY()));
						i++;
						goUp--;
					}
				}
				
				return scan8no;
	}
	
	private static ArrayList<Path> extendsO0(ArrayList<Path> scan4){
		//conversione scanpath 8x8
		
				ArrayList<Path> scan8ne = new ArrayList<Path>();

				
				for (Path p : scan4) {
					scan8ne.add(new Path(p.getX(),p.getY()+4));
				}
				
				int i=15;
				int n=8;
				int goRx=0,goUp=0,goDown=0,goLx=0;
				
				// scan path north-east
				
				while( i < ((n*n)-1) ){
					Path currentPath = scan8ne.get(i);
					if(currentPath.getY() == n-1){ // cambio riga
						scan8ne.add(new Path(currentPath.getX()+1, currentPath.getY()));
						scan8ne.add(new Path(currentPath.getX()+1, currentPath.getY()-1));
						goLx=currentPath.getX();
						goUp = currentPath.getX()+1;
						goDown=currentPath.getX()+1;
						goRx=currentPath.getX()+2;
						i= i+2;
					}else if( goLx != 0 ){ // vado a sinistra
						scan8ne.add(new Path(currentPath.getX(), currentPath.getY()-1));
						i++;
						goLx--;
						
					}else if(goUp != 0){ // salgo
						scan8ne.add(new Path(currentPath.getX()-1, currentPath.getY()));
						i++;
						goUp--;
					}else if(goUp == 0 && currentPath.getX() == 0){//cambio colonna
						scan8ne.add(new Path(currentPath.getX(), currentPath.getY()-1));
						scan8ne.add(new Path(currentPath.getX()+1, currentPath.getY()-1));
						i= i+2;
					}else if(goDown != 0 ){// scendo
						scan8ne.add(new Path(currentPath.getX()+1, currentPath.getY()));
						i++;
						goDown--;
					}else if(goRx != 0){ // vado a destra
						scan8ne.add(new Path(currentPath.getX(), currentPath.getY()+1));
						i++;
						goRx--;
					}
				}
				
				return scan8ne;
	}
	
	private static void scanOnImage(ArrayList<Path> scan8, Block block, int[][] matrix, String direction) {
		int i=0;
		int x1=0,y1=0,posx=0,posy=0,x2=0,y2=0,xStart=0,yStart=0;
		Path p = scan8.get(i); 
		
		
		if(direction.equals(NORTH_EAST)){
			xStart=block.getxEnd();
			yStart=block.getyStart();
		}else if(direction.equals(NORTH_OVEST)){
			xStart=block.getxStart();
			yStart=block.getyStart();
		}else if(direction.equals(SOUTH_OVEST)){
			xStart=block.getxStart();
			yStart=block.getyEnd();
		}else{
			xStart=block.getxEnd();
			yStart=block.getyEnd();
		}
		
		//non controllo che parto da noth-east poi va inserito
		int pixel = matrix[xStart][yStart];
		System.out.println("["+ xStart+"]["+yStart +"]:" + pixel);
		x1=p.getX();
		y1=p.getY();
		posx=xStart;
		posy=yStart;
		
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
	
	public static void populate(ArrayList<Path> scan,String scan4){
		while(scan4.length()!= 0){
			char xy[] = new char[2];
			scan4.getChars(0, 2, xy, 0);
			scan4 = scan4.substring(2);
			scan.add(new Path( Integer.parseInt(""+xy[0]), Integer.parseInt(""+xy[1])));
		}

	}

}
