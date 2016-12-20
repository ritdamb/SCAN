package model;

import java.util.ArrayList;

public class Block {
	private int xStart;
	private int xEnd;
	private int yStart;
	private int yEnd;

	public Block(int xStart, int xEnd, int yStart, int yEnd) {
		super();
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
	}

	public int getxStart() {
		return xStart;
	}

	public int getxEnd() {
		return xEnd;
	}

	public int getyStart() {
		return yStart;
	}

	public int getyEnd() {
		return yEnd;
	}
	
	public int length(){
		return xEnd - xStart + 1;
	}

	public Block() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setxStart(int xStart) {
		this.xStart = xStart;
	}

	public void setxEnd(int xEnd) {
		this.xEnd = xEnd;
	}

	public void setyStart(int yStart) {
		this.yStart = yStart;
	}

	public void setyEnd(int yEnd) {
		this.yEnd = yEnd;
	}
	
	public String toString(){
		return "xStart="+xStart+" xEnd="+xEnd+" yStart="+yStart+" yEnd="+yEnd;
	}
	
	public static ArrayList<Block> getBlocks (int matrix[][], int N){
		int len = matrix.length;
		ArrayList<Block> blocks = new ArrayList<Block>();

		//# di blocchi che entrano in larghezza e/o in altezza (dato che la matrice � quadrata)
		int numOfBlockPerLine = len/N;
		for(int i=1; i <= numOfBlockPerLine; i++){
			if(i%2 != 0){ //scorro da destra a sinistra
				for(int j=1; j<= numOfBlockPerLine; j++){
					int xStart = (N*(i-1));
					int xEnd = (N*i)-1;
					int yStart = (len-1)-((N*j)-1);
					int yEnd = (len-1)-(N*(j-1));		
					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
			else{ //else scorro da sinistra a destra
				for(int j=numOfBlockPerLine; j>=1; j--){
					int xStart = (N*(i-1));
					int xEnd = (N*i)-1;
					int yStart = (len-1)-((N*j)-1);
					int yEnd = (len-1)-(N*(j-1));
					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
		}
		for(int i=0; i<blocks.size(); i++){
			Block b = blocks.get(i);
		}
		return blocks;
	}
	
	public static Block[] splitBlock(Block block) {
		int xStart = block.getxStart();
		int xEnd = block.getxEnd();
		int yStart = block.getyStart();
		int yEnd = block.getyEnd();

		int newLength = block.length()/2;
		Block subRegions[] = new Block[4];
		subRegions[0] = new Block(xStart, (xStart + newLength -1), (yStart + newLength), yEnd );
		subRegions[1] = new Block(xStart, (xStart + newLength -1), yStart, (yStart + newLength - 1));
		subRegions[2] = new Block((xStart + newLength), xEnd, yStart, (yStart + newLength -1));
		subRegions[3] = new Block((xStart + newLength), xEnd, (yStart + newLength), yEnd);

		return subRegions;
	}
	
}
