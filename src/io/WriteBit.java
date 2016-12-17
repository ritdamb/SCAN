package io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
public class WriteBit {

	public static void main(String[] args){
		
		String header1="111";
		String scanpath="100110111000001001010110001111010000000000101";
		FileOutputStream file=null;
		try {
			file = new FileOutputStream("compress");
			BitOutputStream out = new BitOutputStream(file);
			
			String toWrite = header1+scanpath;
			System.out.println("bit da scrivere : " + toWrite.length());
			for(int i=0; i < toWrite.length(); i++)
				if(toWrite.charAt(i) == '1')
					out.write(1);
				else
					out.write(0);
			
			out.close();
			
			BitInputStream in = new BitInputStream(new FileInputStream("compress"));
			
			int i = in.read();
			while(i != -1 ){
				System.out.print(i);
				i = in.read();
			}
			System.out.println();
			System.out.println(toWrite);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
