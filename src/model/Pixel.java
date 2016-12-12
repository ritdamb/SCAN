package model;

public class Pixel{
	public int x,y;
	
	public Pixel(int x,int y){
		this.x=x;
		this.y=y;
	}
	public int getX(){return x;}
	public int getY(){return y;}
	
	public String toString(){ return "("+this.x+","+this.y+")"; }


}
