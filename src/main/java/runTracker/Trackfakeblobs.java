package runTracker;

import java.util.ArrayList;

import fakeblobs.Addnoise;
import ij.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import poissonSimulator.Poissonprocess;

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
		final int numframes = 15;
		
	
		
		for (int frame = 0; frame < numframes; ++frame){
			
		RandomAccessibleInterval<FloatType> blobimage = new ArrayImgFactory<FloatType>().create(range, new FloatType());
		RandomAccessibleInterval<FloatType> noisyblobs = new ArrayImgFactory<FloatType>().create(range, new FloatType());
		Addnoise.SaltandPepperNoise(blobimage);
		
		fakeblobs.Makespots.Createspots(blobimage, sigma, frame * 10, range, numblobs);
	
		noisyblobs = Poissonprocess.poissonProcess(blobimage, 35);
		
		ImageJFunctions.show(noisyblobs);
		
		
		}
	}
}
