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

	public Pixel transform (int a, int b){ //aggiunge a e b a pixel.x e pixel.y
		return new Pixel(this.x + a, this.y + b);
	}

}
