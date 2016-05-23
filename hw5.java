/* Name:

   UID:

   Others With Whom I Discussed Things:

   Other Resources I Consulted:
   
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// a marker for code that you need to implement
class ImplementMe extends RuntimeException {}

// an RGB triple
class RGB {
    public int R, G, B;

    RGB(int r, int g, int b) {
    	R = r;
		G = g;
		B = b;
    }

    public String toString() { return "(" + R + "," + G + "," + B + ")"; }

   

}


// This class is used to compute mirror image by fork/join
class MirrorTask extends RecursiveTask< RGB[] > {
	private RGB[] originalPixels;
	private int low, // the first pixel of the lowest row
				high; // the first pixel of the highest row
	private int width;

    public MirrorTask(RGB[] a, int l, int h, int w) {
    	this.originalPixels = a;
    	//this.mirrorPixels = a; // Initialize the mirrorPixels to be the same as original pixels
    	this.low = l;
    	this.high = h;
    	this.width = w;
    }

    public RGB[] compute() {
    	// Process image row by row
    	// so SEQUENTIAL_CUTOFF is the length of the width
    	if(high - low > width) { 
    		int lowRowNum = low / width;
    		int highRowNum = high / width;
    		int midRowNum = (lowRowNum + highRowNum) / 2 + 1;
    		int mid = midRowNum * width;
    		MirrorTask left = new MirrorTask(originalPixels, low, mid - 1, width);
    		MirrorTask right = new MirrorTask(originalPixels, mid, high, width);
    		left.fork();
    		right.fork();
    		RGB[] leftMirror = left.join();
    		RGB[] rightMirror = right.join();

    		// Get left and right and combine them into larger image
    		int currentWidth = mid - low;
    		int combinedWidth = high - low + 1;
    		RGB[] combinedMirror = new RGB[combinedWidth];

    		// Add leftMirror
    		for(int i = 0; i < currentWidth; i++) {
    			combinedMirror[i] = new RGB(leftMirror[i].R, 
						    				leftMirror[i].G,
						    				leftMirror[i].B);
	    	}

	    	// Add rightMirror
	    	for(int j = mid; j < combinedWidth; j++) {
	    		combinedMirror[j] = new RGB(rightMirror[j-currentWidth].R, 
							    			rightMirror[j-currentWidth].G,
							    			rightMirror[j-currentWidth].B);
	    	}
    		return combinedMirror;
    		
/* 
    		RGB[] leftMirror = left.join();
    		RGB[] rightMirror = right.join();

			RGB[] mirrorPixels = new RGB[originalPixels.length];
    		for(int i = low; i < mid; i++) {
    			mirrorPixels[i].R = leftMirror[i].R;
    			mirrorPixels[i].G = leftMirror[i].G;
    			mirrorPixels[i].B = leftMirror[i].B;
    		}

    		for(int j = mid; j < high; j++) {
    			mirrorPixels[j].R = leftMirror[j].R;
    			mirrorPixels[j].G = leftMirror[j].G;
    			mirrorPixels[j].B = leftMirror[j].B;
    		}

    		return mirrorPixels;*/
    	}
    	else {
    		RGB[] oneRowOfPixels = new RGB[width]; // Construct one row of pixels
    		for(int i = low; i < high+1; i++){
    			int index = i - low;
    			int mirrorIndex = high - index;
    			oneRowOfPixels[index] = new RGB(originalPixels[mirrorIndex].R,
    											originalPixels[mirrorIndex].G,
    											originalPixels[mirrorIndex].B);
    		}

    		return oneRowOfPixels;
    		/*
    		RGB[] mirrorPixels = new RGB[originalPixels.length];
    		for(int i = low; i < high; i++) {
    			int origIndex = high - 1 - (low - i);
    			mirrorPixels[i] = new RGB(originalPixels[origIndex].R, 
						    				originalPixels[origIndex].G, 
						    				originalPixels[origIndex].B);
    		}

    		return mirrorPixels; */
    	}
    }
}
	


// an object representing a single PPM image
class PPMImage {
    protected int width, height, maxColorVal;
    protected RGB[] pixels;

    public PPMImage(int w, int h, int m, RGB[] p) {
		width = w;
		height = h;
		maxColorVal = m;
		pixels = p;
    }

    // parse a PPM image file named fname and produce a new PPMImage object
    public PPMImage(String fname) 
    	throws FileNotFoundException, IOException {
		FileInputStream is = new FileInputStream(fname);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		br.readLine(); // read the P6
		String[] dims = br.readLine().split(" "); // read width and height
		int width = Integer.parseInt(dims[0]);
		int height = Integer.parseInt(dims[1]);
		int max = Integer.parseInt(br.readLine()); // read max color value
		br.close();

		is = new FileInputStream(fname);
	    // skip the first three lines
		int newlines = 0;
		while (newlines < 3) {
	    	int b = is.read();
	    	if (b == 10)
				newlines++;
		}

		int MASK = 0xff;
		int numpixels = width * height;
		byte[] bytes = new byte[numpixels * 3];
        is.read(bytes);
		RGB[] pixels = new RGB[numpixels];
		for (int i = 0; i < numpixels; i++) {
	    	int offset = i * 3;
	    	pixels[i] = new RGB(bytes[offset] & MASK, 
	    						bytes[offset+1] & MASK, 
	    						bytes[offset+2] & MASK);
		}
		is.close();

		this.width = width;
		this.height = height;
		this.maxColorVal = max;
		this.pixels = pixels;
    }

	// write a PPMImage object to a file named fname
    public void toFile(String fname) throws IOException {
		FileOutputStream os = new FileOutputStream(fname);

		String header = "P6\n" + width + " " + height + "\n" 
						+ maxColorVal + "\n";
		os.write(header.getBytes());

		int numpixels = width * height;
		byte[] bytes = new byte[numpixels * 3];
		int i = 0;
		for (RGB rgb : pixels) {
	    	bytes[i] = (byte) rgb.R;
	    	bytes[i+1] = (byte) rgb.G;
	    	bytes[i+2] = (byte) rgb.B;
	    	i += 3;
		}
		os.write(bytes);
		os.close();
    }

	// implement using Java 8 Streams
    public PPMImage negate() {
		RGB[] negatePixels = new RGB[this.pixels.length];
		
		// Inititalize array negatePixels
		for(int i = 0; i < this.pixels.length; i++)
			negatePixels[i] = new RGB(this.pixels[i].R, this.pixels[i].G, this.pixels[i].B);

		Arrays.stream(negatePixels)
				.parallel()
				.forEach(rgb -> {
					rgb.R = this.maxColorVal - rgb.R;
					rgb.G = this.maxColorVal - rgb.G;
					rgb.B = this.maxColorVal - rgb.B;
				});

		return new PPMImage(width, height, maxColorVal, negatePixels);
    }

	// implement using Java 8 Streams
    public PPMImage greyscale() {
		RGB[] greyPixels = new RGB[this.pixels.length];
		
		// Initialize array greyPixels
		for(int i = 0; i < this.pixels.length; i++)
			greyPixels[i] = new RGB(this.pixels[i].R, this.pixels[i].G, this.pixels[i].B);

		Arrays.stream(greyPixels)
				.parallel()
				.forEach(rgb -> {
					rgb.R = (int) Math.round(.299 * rgb.R + .587 * rgb.G + .114 * rgb.B);
					rgb.G = rgb.R; 
					rgb.B = rgb.R;
				});

		return new PPMImage(width, height, maxColorVal, greyPixels);
    }    
    
	// implement using Java's Fork/Join library
    public PPMImage mirrorImage() {
    	//int low = 0; // low = the first pixel of the lowest row
    	//int high = this.pixels.length - this.width; // high = the first pixel of the highest row
		//MirrorTask newPixels = new MirrorTask(pixels, low, high, width);
		MirrorTask newPixels = new MirrorTask(pixels, 0, pixels.length - 1, width);

		// Compute mirror image (fork/join method in compute() function)
		RGB[] mirrorPixels = newPixels.compute();

		return new PPMImage(width, height, maxColorVal, mirrorPixels);
    }

    // Convert RGB to integer
	// Example: 
	// Input: RGB = (255, 10, 3)
	// Output: 255010003
	private int toInteger(RGB rgb) { return (rgb.R * 1000000 + rgb.G * 1000 + rgb.B); }

    // Convert integer into RGB
	// Example:
	// Input: 255010003
	// Output: RGB = (255, 10, 3)
	private RGB toRGB(int inte) {
	    int r = inte / 1000000;
	    int g = (inte % 1000000) / 1000;
	    int b = (inte % 1000000) % 1000;
	    return new RGB(r, g, b);
	} 

	// implement using Java 8 Streams
    public PPMImage mirrorImage2() {
		int[] original = new int[pixels.length];
		int[] mirrorInt = new int[pixels.length];
		RGB[] mirrorRGB = new RGB[pixels.length];


		
		// Generate a stream of integers, one per pixel
		IntStream.range(0, pixels.length)
					.parallel()
					.forEach(i -> original[i] = toInteger(pixels[i])); 

		// Process these integers in parallel
		IntStream.range(0, pixels.length)
					.parallel()
					.forEach(i -> {
						int col = i % width;
						int theLastElementOfThisRow = (i / width + 1) * width - 1;
						int mirrorIndex = theLastElementOfThisRow - col;
						mirrorInt[i] = original[mirrorIndex];
						//System.out.println("i: " + i + "	mirror i: " + mirrorIndex);
					});

		// Convert integer array back to RGB array
		IntStream.range(0, pixels.length)
					.parallel()
					.forEach(i -> {
						mirrorRGB[i] = toRGB(mirrorInt[i]);
					});
			
		return new PPMImage(width, height, maxColorVal, mirrorRGB);
    }

	// implement using Java's Fork/Join library
    public PPMImage gaussianBlur(int radius, double sigma) {
		throw new ImplementMe();
    }

}

// code for creating a Gaussian filter
class Gaussian {

    protected static double gaussian(int x, int mu, double sigma) {
		return Math.exp( -(Math.pow((x-mu)/sigma,2.0))/2.0 );
    }

    public static double[][] gaussianFilter(int radius, double sigma) {
		int length = 2 * radius + 1;
		double[] hkernel = new double[length];
		for(int i=0; i < length; i++)
	    	hkernel[i] = gaussian(i, radius, sigma);
		double[][] kernel2d = new double[length][length];
		double kernelsum = 0.0;
		for(int i=0; i < length; i++) {
	    	for(int j=0; j < length; j++) {
				double elem = hkernel[i] * hkernel[j];
				kernelsum += elem;
				kernel2d[i][j] = elem;
	    	}
		}
		for(int i=0; i < length; i++) {
	    	for(int j=0; j < length; j++)
				kernel2d[i][j] /= kernelsum;
		}
		return kernel2d;
    }
}

class Main {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		PPMImage image = new PPMImage("florence.ppm");
		/*PPMImage negateImage = image.negate();
		negateImage.toFile("florence_negate.ppm");

		PPMImage greyImage = image.greyscale();
		greyImage.toFile("florence_greyScale.ppm");

		PPMImage mirror = image.mirrorImage();
		mirror.toFile("florence_mirror.ppm");

		*/
		/*
		int[] left = new int[10];
		left[0] = 1;
		left[1] = 2;
		left[2] = 3;
		left[3] = 4;
		left[4] = 5;

		int[] right = new int[] {6, 7, 8, 9, 10};
		for(int i = 5; i < 10; i++){
			left[i] = right[i - 5];
		}

		for(int i = 0; i < 10; i++)
			System.out.print(left[i] + " ");*/

/*		RGB[] testRGB = new RGB[16];
		for(int i = 0; i < 16; i++){
			testRGB[i] = new RGB(i, i, i);
			//System.out.println(testRGB[i]);
		}
			

		PPMImage imageTest = new PPMImage(4, 4, 225, testRGB);
		PPMImage testImage = imageTest.mirrorImage2();*/

		PPMImage mirror2 = image.mirrorImage2();
		mirror2.toFile("florence_mirror2.ppm");
		
	}

}

