package ac;
/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Compression application using adaptive arithmetic coding.
 * <p>Usage: java AdaptiveArithmeticCompress InputFile OutputFile</p>
 * <p>Then use the corresponding "AdaptiveArithmeticDecompress" application to recreate the original input file.</p>
 * <p>Note that the application starts with a flat frequency table of 257 symbols (all set to a frequency of 1),
 * and updates it after each byte encoded. The corresponding decompressor program also starts with a flat
 * frequency table and updates it after each byte decoded. It is by design that the compressor and
 * decompressor have synchronized states, so that the data can be decompressed properly.</p>
 */
public class AdaptiveArithmeticCompress {
	
	private ArrayList<Integer> stream;
	
	public AdaptiveArithmeticCompress(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java AdaptiveArithmeticCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Perform file compression
		try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
			try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
				compress(in, out);
			}
		}
	}
	
	public AdaptiveArithmeticCompress() {
		super();
	}
	
	public void compress(ArrayList<Integer> list) throws IOException{
		try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new ByteArrayOutputStream()))) {
			compress(list, out);
		}
	}
	
	//custom compress
	private void compress(ArrayList<Integer> list, BitOutputStream out) throws IOException {
			FlatFrequencyTable initFreqs = new FlatFrequencyTable(513);
			FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
			ArithmeticEncoder enc = new ArithmeticEncoder(out);
			for(Integer symbol : list) {
				// Read and encode one byte
				symbol +=255;
				enc.write(freqs, symbol);
				freqs.increment(symbol);
			}
			enc.write(freqs, 512);  // EOF
			enc.finish();  // Flush remaining code bits
			
			stream = enc.getStream();
		}
	
	// To allow unit testing, this method is package-private instead of private.
	static void compress(InputStream in, BitOutputStream out) throws IOException {
		FlatFrequencyTable initFreqs = new FlatFrequencyTable(257);
		FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
		ArithmeticEncoder enc = new ArithmeticEncoder(out);
		while (true) {
			// Read and encode one byte
			int symbol = in.read();
			if (symbol == -1)
				break;
			enc.write(freqs, symbol);
			freqs.increment(symbol);
		}
		enc.write(freqs, 256);  // EOF
		enc.finish();  // Flush remaining code bits
	}
	
	public ArrayList<Integer> getStream(){
		return stream;
	}
	
}
