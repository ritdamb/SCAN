package model;

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
	
	
}
