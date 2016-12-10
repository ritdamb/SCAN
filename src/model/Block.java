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
}
