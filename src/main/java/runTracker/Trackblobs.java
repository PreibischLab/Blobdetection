package runTracker;

import java.io.File;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
import blobObjects.FramedBlob;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import overlaytrack.DisplayBlobs;
import overlaytrack.DisplayGraph;
import segmentBlobs.Staticproperties;
import trackerType.KFsearch;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util
				.openAs32Bit(new File("src/main/resources/C1-test.tif"), new ArrayImgFactory<FloatType>());
		
		final RandomAccessibleInterval<FloatType> preprocessedimg = util.ImgLib2Util
				.openAs32Bit(new File("src/main/resources/C1-test-pre.tif"), new ArrayImgFactory<FloatType>());
		
		
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		Normalize.normalize(Views.iterable(preprocessedimg), minval, maxval);
		ImagePlus imp = ImageJFunctions.show(img);
		
		final boolean softThreshold = true;
		// Noisye: 15stackimage.tif
		// Highly Noisy: 15HighNoisyblobs.tif
		// src/main/resources/C1-test-pre.tif
		// Actual data
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET-brightnessadjust.tif
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET.tif
		// Display stack
		//ImageJFunctions.show(img);
		
		final int minDiameter = 5;
		final int maxDiameter = 40;
		final double[] calibration = {imp.getCalibration().pixelWidth, imp.getCalibration().pixelHeight, imp.getCalibration().pixelDepth};
		final int maxframe = (int)img.dimension(ndims - 1) ;
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();
		
		
		for (int i = 0; i < maxframe ; ++i) {
			IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);
			IntervalView<FloatType> currentframepreprocessed = Views.hyperSlice(preprocessedimg, ndims - 1, i);
			
			RandomAccessibleInterval<FloatType> preinputimg = new ArrayImgFactory<FloatType>().create(currentframe,
					new FloatType());
			preinputimg = preProcessingTools.Kernels.Meanfilterandsupress(currentframepreprocessed, 2.0);
			ArrayList<Staticproperties> Spotmaxbase =  Makebloblist.returnBloblist(currentframe,preinputimg, minDiameter, maxDiameter, calibration, i , softThreshold);
			Allspots.add( i, Spotmaxbase );
			System.out.println("Finding blobs in Frame: " + i);
			System.out.println("Total number of Blobs found: " + Spotmaxbase.size());
		}
		
		
		
		// Either perform Nearest Neighbour tracking or Kalman Filter tracking
/*		
			// Create an object for NN search
			final double maxsqdistance = 1000;
			NNsearch  NNsearchsimple = new NNsearch(Allspots, maxsqdistance, img.dimension(ndims - 1)  );
			NNsearchsimple.process();
			SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = NNsearchsimple.getResult();
			System.out.println("NN search process done");
*/
			// Create an object for Kalman Filter tracking
			final int initialSearchradius = 0;
			final int maxSearchradius = 30;
			final int missedframes = maxframe / 10;
		
		    KFsearch KFsimple = new KFsearch(Allspots, initialSearchradius, maxSearchradius, (int)img.dimension(ndims - 1), missedframes);
	        KFsimple.process();
	        System.out.println("KF search process done");
		    SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = KFsimple.getResult();
		    ArrayList<FramedBlob> frameandblob = KFsimple.getFramelist();
		    
		    
		    // Overlay the track on the stack
	   	/*   
			if( graph!= null){
			DisplayGraph displaytracks = new DisplayGraph(imp, graph, ndims - 1);
			displaytracks.getImp();
			}
			
		*/
			RandomAccessibleInterval<FloatType> detimg = new ArrayImgFactory<FloatType>().create(img,
					new FloatType());
			
			if (frameandblob.size() > 0)
			DisplayBlobs.Displaydetection(detimg, frameandblob);
			Normalize.normalize(Views.iterable(detimg), minval, maxval);
			ImageJFunctions.show(detimg);
	}

	

}
