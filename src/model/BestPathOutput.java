package model;

import java.util.ArrayList;

public class BestPathOutput {
	String bestPathName;
	Path bestPath; //best path P scelto
	public Path getBestPath() {
		return bestPath;
	}

	public void setBestPath(Path bestPath) {
		this.bestPath = bestPath;
	}

	int E; //Sum E of absolute values of prediction errors along P
	int B; //Numero di bit necessari per la codifica di P
	ArrayList<Integer> L; //Sequence L of prediction errors along P
	private Pixel lastPixel;
	
	public BestPathOutput(String bestPathName, Path bestPath, int a, int b, ArrayList<Integer> l, Pixel lp) {
		super();
		this.bestPathName = bestPathName;
		this.bestPath = bestPath;
		E = a;
		B = b;
		L = l;
		setLastPixel(lp);
	}

	public BestPathOutput() {
		super();
	}

	public void setBestPathName(String bestPathName) {
		this.bestPathName = bestPathName;
	}

	public void setE(int e) {
		E = e;
	}

	public void setB(int b) {
		B = b;
	}

	public void setL(ArrayList<Integer> l) {
		L = l;
	}

	public String getBestPathName() {
		return bestPathName;
	}

	public int getE() {
		return E;
	}

	public int getB() {
		return B;
	}

	public ArrayList<Integer> getL() {
		return L;
	}

	public Pixel getLastPixel() {
		return lastPixel;
	}

	public void setLastPixel(Pixel lastPixel) {
		this.lastPixel = lastPixel;
	}
	
	
	
	
}
