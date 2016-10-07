package runTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
import blobObjects.FramedBlob;
import blobObjects.Subgraphs;
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
import overlaytrack.DisplaysubGraph;
import segmentBlobs.Staticproperties;
import trackerType.KFsearch;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util
				.openAs32Bit(new File("/Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET-brightnessadjust.tif"), new ArrayImgFactory<FloatType>());
		
		final RandomAccessibleInterval<FloatType> preprocessedimg = util.ImgLib2Util
				.openAs32Bit(new File("/Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET-brightnessadjust-pre.tif"), new ArrayImgFactory<FloatType>());
		
		
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		Normalize.normalize(Views.iterable(preprocessedimg), minval, maxval);
		ImagePlus imp = ImageJFunctions.show(img);
		
		final boolean softThreshold = false;
		// Noisye: 15stackimage.tif
		// Highly Noisy: 15HighNoisyblobs.tif
		// ../res/C1-test-pre.tif
		// Actual data
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET-brightnessadjust.tif
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET.tif
		// Display stack
		//ImageJFunctions.show(img);
		
		final int minDiameter = 5;
		final int maxDiameter = 50;
		final double[] calibration = {imp.getCalibration().pixelWidth, imp.getCalibration().pixelHeight, imp.getCalibration().pixelDepth};
		final int maxframe = (int)img.dimension(ndims - 1) ;
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();
		
		
		for (int i = 0; i < maxframe ; ++i) {
			IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);
			IntervalView<FloatType> currentframepre = Views.hyperSlice(preprocessedimg, ndims - 1, i);
			
		
			ArrayList<Staticproperties> Spotmaxbase =  Makebloblist.returnBloblist(currentframe,currentframepre, minDiameter, maxDiameter, calibration, i , softThreshold);
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
			final int initialSearchradius = 50;
			final int maxSearchradius = 20;
			final int missedframes = 10;
		
		    KFsearch KFsimple = new KFsearch(Allspots, initialSearchradius, maxSearchradius, (int)img.dimension(ndims - 1), missedframes);
	        KFsimple.process();
	        System.out.println("KF search process done");
		    ArrayList<Subgraphs> subgraph = KFsimple.getFramedgraph();
		    ArrayList<FramedBlob> frameandblob = KFsimple.getFramelist();
		   
		  
		    DisplaysubGraph displaytracks = new DisplaysubGraph(imp, subgraph);
		    displaytracks.getImp();
		    
		
			RandomAccessibleInterval<FloatType> detimg = new ArrayImgFactory<FloatType>().create(img,
					new FloatType());
			
			DisplayBlobs.Displaydetection(detimg, frameandblob);
			
			
			Normalize.normalize(Views.iterable(detimg), minval, maxval);
			ImageJFunctions.show(detimg);
	}
}
