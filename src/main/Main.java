package main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


public class Main {


	public static void main(String[] args) throws IOException{
		if(args.length == 0){
			System.out.println("Usage:");
			System.out.println("-c [compress] -d [decompress] inputFile outputFile");
			return;
		}
		
		if((args.length == 1 && args[0].equals("-h"))){
			if(args[0].equals("-h")){
				System.out.println("Usage:");
				System.out.println("-c [compress] -d [decompress] inputFile outputFile");
				return;
			}
		}

		else {
			if(args.length < 2 || args.length > 2)
				System.out.println("Bad Parameters");
			else{
				if(args[0].equals("-c")){
					System.out.println("Compressing...");
					String inputFile = args[1];
					String outputFile = args[2];

					long startTime = System.currentTimeMillis();

					CompressSCAN compressor = new CompressSCAN(inputFile, outputFile);
					compressor.compress(); // crea il file compress

					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					System.out.println("Elapsed Time: "+elapsedTime/1000 +" seconds");

					File file1 = new File(inputFile);
					File file2 = new File(outputFile);
					double file1Size = file1.length();
					double file2Size = file2.length();
					double ratio = file1Size/file2Size;
					DecimalFormat four = new DecimalFormat("#0.0000");
					System.out.println("Compression ratio: "+four.format(ratio));
				}
				else if(args[0].equals("-d")){
					System.out.println("Decompressing...");
					String inputFile = args[1];
					String outputFile = args[2];

					long startTime = System.currentTimeMillis();

					DecompressSCAN decompressor = new DecompressSCAN(inputFile, outputFile);
					decompressor.decompress();

					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					System.out.println("Elapsed Time: "+elapsedTime/1000 +" seconds");
				}
			}
		}

	}


}
