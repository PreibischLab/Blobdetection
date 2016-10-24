package runTracker;

import java.io.File;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import overlaytrack.DisplayGraph;
import preProcessingTools.Kernels;
import preProcessingTools.MedianFilter2D;
import segmentBlobs.GetLocalmaxmin;
import segmentBlobs.GlobalThresholding;
import segmentBlobs.Staticproperties;
import trackerType.NNsearch;

public class TrackblobsNNsearchGauss {
	public static void main(String[] args) throws Exception {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util.openAs32Bit(
				new File(
						"/Users/varunkapoor/Documents/Pierre_data/Recording_Cell_Culture_Kapoor/mCherry_ShortET-secdup-short.tif"),
				new ArrayImgFactory<FloatType>());

		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);

		ImagePlus imp = ImageJFunctions.show(img);

		final int maxframe = (int) img.dimension(ndims - 1);
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();
		System.out.println("Appliying Median filter to the stack");
		final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(img, 1);
		medfilter.process();
		final RandomAccessibleInterval<FloatType> preprocessedimage = medfilter.getResult();
		Normalize.normalize(Views.iterable(preprocessedimage), minval, maxval);
		ImageJFunctions.show(preprocessedimage);
		System.out.println("Median filter sucessfully applied");
		for (int i = 0; i < maxframe; ++i) {
			IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);

			 final RandomAccessibleInterval<FloatType> currentframepre =  Views.hyperSlice(preprocessedimage, ndims - 1, i);
				
				
				
				final Float threshold = GlobalThresholding.AutomaticThresholding(currentframepre);
				Float val = new Float(threshold);
				final Img<BitType> bitimg = new ArrayImgFactory<BitType>().create(currentframe, new BitType());
				GetLocalmaxmin.ThresholdingBit(currentframepre, bitimg, val);

			
			// To get Blobs via LM fit and Gaussian detection, comment the two
			// lines below if using DoG detection
			ArrayList<Staticproperties> Spotmaxbase = Makebloblist.returnRefinedBloblist(currentframe, currentframepre, bitimg,
					i);

			Allspots.add(i, Spotmaxbase);
			System.out.println("Finding blobs in Frame: " + i);
			System.out.println("Total number of Blobs found: " + Spotmaxbase.size());
		}

		// Perform Nearest Neighbour tracking

		// Create an object for NN search
		final double maxsqdistance = 1000;
		NNsearch NNsearchsimple = new NNsearch(Allspots, maxsqdistance, img.dimension(ndims - 1));
		NNsearchsimple.process();
		SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = NNsearchsimple.getResult();
		System.out.println("NN search process done");

		// Display the result as a graph

		DisplayGraph totaldisplaytracks = new DisplayGraph(imp, graph);
		totaldisplaytracks.getImp();
	}
}
