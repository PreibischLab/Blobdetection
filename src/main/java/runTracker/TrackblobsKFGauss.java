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
import costMatrix.CostFunction;
import costMatrix.IntensityDiffCostFunction;
import costMatrix.SquareDistCostFunction;
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
import preProcessingTools.Kernels;
import preProcessingTools.MedianFilter2D;
import segmentBlobs.Staticproperties;
import trackerType.KFsearch;

public class TrackblobsKFGauss {

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
		ImagePlus impcopy = ImageJFunctions.show(img);

		
		// Cost function
		/**
		 * Choose one of the cost functions
		 */
		// Distance based Cost function (uncomment the method if has to be used)

		final CostFunction<Staticproperties, Staticproperties> DistCostFunction = new SquareDistCostFunction();

		// Intensity based Cost function (Comment out the method if previous
		// method is being used)
		final CostFunction<Staticproperties, Staticproperties> IntensityCostFunction = new IntensityDiffCostFunction();

		final int maxframe = (int) img.dimension(ndims - 1);
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();

		for (int i = 0; i < maxframe; ++i) {
			IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);

			final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(currentframe, 1);
			medfilter.process();
			final RandomAccessibleInterval<FloatType> inputimg = medfilter.getResult();
			RandomAccessibleInterval<FloatType> preprocessedimg = Kernels.Supressthresh(inputimg);
			Normalize.normalize(Views.iterable(preprocessedimg), minval, maxval);

			RandomAccessibleInterval<FloatType> currentframepre = preprocessedimg;

			// To get Blobs via LM fit and Gaussian detection, comment the two
			// lines below if using DoG detection
			final int maxDiameter = 100;
			ArrayList<Staticproperties> Spotmaxbase = Makebloblist.returnRefinedBloblist(currentframe, currentframepre,
					i, maxDiameter);

			Allspots.add(i, Spotmaxbase);
			System.out.println("Finding blobs in Frame: " + i);
			System.out.println("Total number of Blobs found: " + Spotmaxbase.size());
		}

		// Create an object for Kalman Filter tracking
		// Initial search radius as maximal distance allowed for initial search
		// to initiate the Kalman Filter tracks
		final int initialSearchradius = 50;
		// For linking costs, this is how far we allow for the blob to move
		final int maxSearchradius = 15;
		final int missedframes = 20;

		KFsearch KFsimple = new KFsearch(Allspots, DistCostFunction, initialSearchradius, maxSearchradius,
				(int) img.dimension(ndims - 1), missedframes);
		KFsimple.process();
		System.out.println("KF search process done");
		ArrayList<Subgraphs> subgraph = KFsimple.getFramedgraph();
		SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph = KFsimple.getResult();
		ArrayList<FramedBlob> frameandblob = KFsimple.getFramelist();

		DisplaysubGraph displaytracks = new DisplaysubGraph(imp, subgraph);
		displaytracks.getImp();

		DisplayGraph totaldisplaytracks = new DisplayGraph(impcopy, graph);
		totaldisplaytracks.getImp();

		RandomAccessibleInterval<FloatType> detimg = new ArrayImgFactory<FloatType>().create(img, new FloatType());

		// Display Blobs if using Gaussian detection
		DisplayBlobs.DisplayRefineddetection(detimg, frameandblob);

		Normalize.normalize(Views.iterable(detimg), minval, maxval);
		ImageJFunctions.show(detimg);
	}
}
