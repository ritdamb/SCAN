package io;

public class Writer {

	public Writer() {
		super();
	}
	
	public void WriteImage (int size, String encScanPath, int pixel1, int pixel2, int n0,int n1, int n2, int n3, String encPredErrors ){
		
		//calcolo N
		int n = (int) (Math.log(size) / Math.log(2));
		String encodedSize =  Integer.toString(n-1,2);
		if(encodedSize.length() == 1)
			encodedSize = "00"+encodedSize;
		if(encodedSize.length() == 2)
			encodedSize = "0"+encodedSize;
		

		String s1 = String.format("%8s", Integer.toBinaryString(pixel1)).replace(' ', '0');
		String s2 = String.format("%8s", Integer.toBinaryString(pixel2)).replace(' ', '0');
		
	}
}
