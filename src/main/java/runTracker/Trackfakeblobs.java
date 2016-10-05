package runTracker;

import java.util.ArrayList;

import fakeblobs.Addnoise;
import ij.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
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
		
		
		fakeblobs.Makespots.Createspots(blobimage, sigma, frame * 10, range, numblobs);
		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(blobimage), minval, maxval);
		
		preProcessingTools.Kernels.addBackground(Views.iterable(blobimage), 0.2);
		
		
		noisyblobs = Poissonprocess.poissonProcess(blobimage, 15);
		
		ImageJFunctions.show(noisyblobs);
		
		
		}
	}
}
