package runTracker;

import java.util.ArrayList;

import ij.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

public class Trackfakeblobs {
	public static void main (String[] args){
        new ImageJ();
		
		final FinalInterval range = new FinalInterval(512, 512);
	
		
		
		
		final int ndims = range.numDimensions();
		final double [] sigma = {15,15};
		final double [] Ci = new double[ndims];
		
		for (int d = 0; d < ndims; ++d)
			Ci[d] = 1.0 / Math.pow(sigma[d],2);
		
		final int numblobs = 15;
		final int numframes = 5;
		
	
		
		for (int frame = 0; frame < numframes; ++frame){
			
		RandomAccessibleInterval<FloatType> blobimage = new ArrayImgFactory<FloatType>().create(range, new FloatType());
			
		fakeblobs.Makespots.Createspots(blobimage, sigma, frame * 10, range, numblobs);
	//	RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage, new IntType());
		
		// Find Maxima of blobs by segmenting the image via watershed
	//	labelledimage = segmentBlobs.Segmentbywatershed.getsegmentedimage(blobimage);
		// List containing all the maximas
	//	ArrayList<RefinedPeak<Point>> SubpixelMinlist = segmentBlobs.Segmentbywatershed.DoGdetection(blobimage, labelledimage, sigma);
		
	//	for(int index = 0; index < SubpixelMinlist.size(); ++index )
	//		System.out.println("Frame: "+ frame  +" " +SubpixelMinlist.get(index));
		
		
		ImageJFunctions.show(blobimage);
		
		
		}
	}
}
