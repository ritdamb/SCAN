package io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ac.BitOutputStream;

public class Writer {

	public Writer() {
		super();
	}
	
	public void writeImage (String pathOutFile, int size, String encScanPath, int[] first_pixel, int[] second_pixel, int n0,int n1, int n2, int n3, ArrayList<Integer> stream ){
		
		//calcolo N
		int n = (int) (Math.log(size) / Math.log(2));
		String encodedSize =  Integer.toString(n-2,2);
		if(encodedSize.length() == 1)
			encodedSize = "00"+encodedSize;
		if(encodedSize.length() == 2)
			encodedSize = "0"+encodedSize;
		
		String header1 = encodedSize;
		
		String s1 = String.format("%8s", Integer.toBinaryString(first_pixel[0])).replace(' ', '0');
		s1 += String.format("%8s", Integer.toBinaryString(first_pixel[1])).replace(' ', '0');
		s1 += String.format("%8s", Integer.toBinaryString(first_pixel[2])).replace(' ', '0');
		
		String s2 = String.format("%8s", Integer.toBinaryString(second_pixel[0])).replace(' ', '0');		
		s2 += String.format("%8s", Integer.toBinaryString(second_pixel[1])).replace(' ', '0');
		s2 += String.format("%8s", Integer.toBinaryString(second_pixel[2])).replace(' ', '0');
		
		String bs0 = String.format("%32s", Integer.toBinaryString(n0)).replace(' ', '0');
		String bs1 = String.format("%32s", Integer.toBinaryString(n1)).replace(' ', '0');
		String bs2 = String.format("%32s", Integer.toBinaryString(n2)).replace(' ', '0');
		String bs3 = String.format("%32s", Integer.toBinaryString(n3)).replace(' ', '0');
		
		String header2=s1+s2+bs0+bs1+bs2+bs3;
		
		String header3="";
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(pathOutFile);
			BitOutputStream out = new BitOutputStream(file);
			
			String toWrite = header1+encScanPath+header2+header3;
			for(int i=0; i < toWrite.length(); i++)
				if(toWrite.charAt(i) == '1')
					out.write(1);
				else
					out.write(0);
			
			
			for(Integer i : stream)
				out.writeByte(i);
			
			
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
