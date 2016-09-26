package runTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
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
import overlaytrack.DisplayGraph;
import segmentBlobs.Getobjectproperties;
import segmentBlobs.Staticproperties;
import trackerType.NNsearch;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util
				.openAs32Bit(new File("src/main/resources/15framenoisyblobs.tif"), new ArrayImgFactory<FloatType>());
		
		
		
		
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
		
			// Create an object for NN search
			NNsearch  NNsearchsimple = new NNsearch(Allspots, maxsqdistance, img.dimension(ndims - 1)  );
			
			NNsearchsimple.process();
			
			SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = NNsearchsimple.getResult();
			if( graph!= null){
			DisplayGraph displaytracks = new DisplayGraph(imp, graph, ndims - 1);
			displaytracks.getImp();
		//	displaytracks.displaytracks();
			}
		
	}

	

}
