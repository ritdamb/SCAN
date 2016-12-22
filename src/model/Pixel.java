package model;

public class Pixel{
	public int x,y;
	public String predictor;
	
	public Pixel(int x,int y){
		this.x=x;
		this.y=y;
	}
	
	public Pixel(int x, int y, String predictor){
		this.x=x;
		this.y=y;
		this.predictor=predictor;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String getPredictor(){
		return predictor;
	}
	
	public void setPredictor(String predictor){
		this.predictor=predictor;
	}
	
	public String toString(){ 
		return "("+this.x+","+this.y+")("+this.predictor+")"; 
	}

	public Pixel transform (int a, int b){ //aggiunge a e b a pixel.x e pixel.y
		String pred="";
		if(a==1 && b==0){
			if(this.predictor.endsWith("L"))
				pred="UL";
			else
				pred="UR";
		}
		else if(a==0 && b==1){
			if(this.predictor.startsWith("U"))
				pred="UL";
			else
				pred="BL";
		}
		else if(a==-1 && b==0){
			if(this.predictor.endsWith("L"))
				pred="BL";
			else
				pred="BR";
		}
		else if(a==0 && b==-1){
			if(this.predictor.startsWith("U"))
				pred="UR";
			else
				pred="BR";
		}
		else if(a==1 && b==1){
			pred="UL";
		}
		else if(a==1 && b==-1)
			pred="UR";
		else if(a==-1 && b==-1){
			pred="BR";
		}
		else if(a==-1 && b==1){
			pred="BL";
		}
		
		return new Pixel(this.x + a, this.y + b, pred);
	}
	
	

}
