package runTracker;

import java.io.File;
import java.util.ArrayList;

import blobList.Makebloblist;
import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import segmentBlobs.Getobjectproperties;
import segmentBlobs.Staticproperties;
import trackerType.NNsearch;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util
				.openAs32Bit(new File("src/main/resources/15stackimage.tif"), new ArrayImgFactory<FloatType>());
	//	ImagePlus imp = ImageJFunctions.show(img);
		
		// add listener to the imageplus slice slider
	//	SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
		
	//	SimpleMultiThreading.threadHaltUnClean();
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		// 5 frame image: 5framestack.tif
		// 15 frame image: 15stackimage.tif
		// Noisy : 15framenoisyblobs.tif
		// Highly Noisy: 15HighNoisyblobs.tif
		
		// Actual data
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET.tif
		// Display stack
		ImageJFunctions.show(img);

		final double maxsqdistance = 100;
		IntervalView<FloatType> globalbaseframe = Views.hyperSlice(img, ndims - 1, 0);
		
		ArrayList<double[]> tracklist = new ArrayList<double[]>();
		for (int i = 1; i < img.dimension(ndims - 1) ; ++i) {

			IntervalView<FloatType> baseframe = Views.hyperSlice(img, ndims - 1, i - 1);
			IntervalView<FloatType> targetframe = Views.hyperSlice(img, ndims - 1, i);
			
			System.out.println("Starting nearest neighbour tracking in :" + " Frame: " + (i - 1) + " and " + i
					+ " out of " + img.dimension(ndims - 1) + " Frames");
			
			ArrayList<Staticproperties> Spotmaxbase = Makebloblist.returnBloblist(baseframe);
			ArrayList<Staticproperties> Spotmaxtarget = Makebloblist.returnBloblist(targetframe);
			// Create an object for NN search
			NNsearch  NNsearchsimple = new NNsearch(baseframe, targetframe, Spotmaxbase, Spotmaxtarget, maxsqdistance );
			
			NNsearchsimple.process();
			NNsearchsimple.getResult();
			
		}
		
		
		
		//overlaytrack.Overlaytrack.Overlaynearest(globalbaseframe, tracklist);
	}

	protected static class ImagePlusListener implements SliceListener
	{
		@Override
		public void sliceChanged(ImagePlus arg0)
		{
			System.out.println( arg0.getCurrentSlice() );
		}		
	}

}
