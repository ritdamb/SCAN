package model;

import java.util.ArrayList;

public class BlockErrorOutput {
	int E; //Sum E of absolute values of prediction errors along P
	ArrayList<Integer> L; //Sequence L of prediction errors along P
	
	
	public BlockErrorOutput() {
		super();
	}


	public BlockErrorOutput(int e, ArrayList<Integer> l) {
		super();
		E = e;
		L = l;
	}
	
	
	public int getE() {
		return E;
	}
	public void setE(int e) {
		E = e;
	}
	public ArrayList<Integer> getL() {
		return L;
	}
	public void setL(ArrayList<Integer> l) {
		L = l;
	}
	
	
}
