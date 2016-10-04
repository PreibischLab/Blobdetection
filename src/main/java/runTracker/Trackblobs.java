package runTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
import blobObjects.FramedBlob;
import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
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
import overlaytrack.DisplayBlobs;
import overlaytrack.DisplayGraph;
import segmentBlobs.Getobjectproperties;
import segmentBlobs.Staticproperties;
import trackerType.KFsearch;
import trackerType.NNsearch;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util
				.openAs32Bit(new File("src/main/resources/smallcherry.tif"), new ArrayImgFactory<FloatType>());
		
		
		
		
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		
		ImagePlus imp = ImageJFunctions.show(img);
		
		
		
		// 5 frame image: 5framestack.tif
		// 15 frame image: 15stackimage.tif
		// Noisy : 15framenoisyblobs.tif
		// Highly Noisy: 15HighNoisyblobs.tif
		
		// Actual data
		// /Users/varunkapoor/Documents/Pierre_data/Latest_video/mCherry_ShortET.tif
		// Display stack
		//ImageJFunctions.show(img);

		final double maxsqdistance = 1000;
		final int minDiameter = 5;
		final int maxDiameter = 40;
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();
		
		
		for (int i = 0; i < img.dimension(ndims - 1) ; ++i) {
			IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);
			Allspots.add( i,  Makebloblist.returnBloblist(currentframe, minDiameter, maxDiameter));
			System.out.println("Finding blobs in Frame: " + i);
		}
		
		
		
		// Either perform Nearest Neighbour tracking or Kalman Filter tracking
/*		
			// Create an object for NN search
			NNsearch  NNsearchsimple = new NNsearch(Allspots, maxsqdistance, img.dimension(ndims - 1)  );
			NNsearchsimple.process();
			SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = NNsearchsimple.getResult();
			System.out.println("NN search process done");
*/
			// Create an object for Kalman Filter tracking
			final int initialSearchradius = 0;
			final int maxSearchradius = 100;
		
		    KFsearch KFsimple = new KFsearch(Allspots, initialSearchradius, maxSearchradius, (int)img.dimension(ndims - 1), 1);
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
			ImageJFunctions.show(detimg);
	}

	

}
