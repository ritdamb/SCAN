package main;

public class Path{
	int x,y;
	
	public Path(int x,int y){
		this.x=x;
		this.y=y;
	}
	public int getX(){return x;}
	public int getY(){return y;}
	
	public String toString(){ return "("+this.x+","+this.y+")"; }


}
