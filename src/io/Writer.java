package io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {

	public Writer() {
		super();
	}
	
	public void writeImage (int size, String encScanPath, int pixel1, int pixel2, int n0,int n1, int n2, int n3, String encPredErrors ){
		
		//calcolo N
		int n = (int) (Math.log(size) / Math.log(2));
		String encodedSize =  Integer.toString(n-2,2);
		if(encodedSize.length() == 1)
			encodedSize = "00"+encodedSize;
		if(encodedSize.length() == 2)
			encodedSize = "0"+encodedSize;
		
		String header1 = encodedSize;
		
		String s1 = String.format("%8s", Integer.toBinaryString(pixel1)).replace(' ', '0');
		String s2 = String.format("%8s", Integer.toBinaryString(pixel2)).replace(' ', '0');
		
		String header2=s1+s2;
		
		FileOutputStream file=null;
		try {
			file = new FileOutputStream("compress");
			BitOutputStream out = new BitOutputStream(file);
			
			String toWrite = header1+encScanPath+header2;
			
			for(int i=0; i < toWrite.length(); i++)
				if(toWrite.charAt(i) == '1')
					out.write(1);
				else
					out.write(0);
			
			out.close();
			
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
