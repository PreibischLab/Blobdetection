package runTracker;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobList.Makebloblist;
import blobObjects.FramedBlob;
import blobObjects.Subgraphs;
import costMatrix.CostFunction;
import costMatrix.IntensityDiffCostFunction;
import costMatrix.SquareDistCostFunction;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import mserMethods.GetDelta;
import mserTree.GetMSERtree;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.MultiThreadedAlgorithm;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponentTree;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import overlaytrack.DisplayBlobs;
import overlaytrack.DisplayGraph;
import overlaytrack.DisplaysubGraph;
import preProcessingTools.Kernels;
import preProcessingTools.MedianFilter2D;
import segmentBlobs.GetLocalmaxmin;
import segmentBlobs.GlobalThresholding;
import segmentBlobs.Staticproperties;
import trackerType.KFSearchRefined;
import trackerType.KFsearch;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class TrackblobsKFGauss {

	public static void main(String[] args) throws Exception {

		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util.openAs32Bit(
				// new File(
				// "/Users/varunkapoor/Downloads/test.jpg"),
				// new ArrayImgFactory<FloatType>());

				// new File(
				// "../res/virusMovie.tif"),
				// new ArrayImgFactory<FloatType>());

				new File(
						"/Users/varunkapoor/Documents/Pierre_data/Recording_Cell_Culture_Kapoor/mCherry_ShortET-secdup-test.tif"),
				new ArrayImgFactory<FloatType>());

		// new File(
		// "/Users/varunkapoor/Documents/Abin_data/Spindle_Image/seb-data.tif"),
		// new ArrayImgFactory<FloatType>());

		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);

		ImagePlus imp = ImageJFunctions.show(img);
		ImagePlus impcopy = ImageJFunctions.show(img);
		final int delta = 20;
		final long minSize = 1;
		final long maxSize = Long.MAX_VALUE;
		final double maxVar = 0.5;
		final double minDiversity = 0;
		final int maxblobs = 40;
		final int maxdelta = 20;
		final boolean darktoBright = false;
		final int mindiameter = 1;

		// Cost function
		/**
		 * Choose one of the cost functions
		 */
		// Distance based Cost function (uncomment the method if has to be used)

		final CostFunction<Staticproperties, Staticproperties> DistCostFunction = new SquareDistCostFunction();

		// Intensity based Cost function (Comment out the method if previous
		// method is being used)
		final CostFunction<Staticproperties, Staticproperties> IntensityCostFunction = new IntensityDiffCostFunction();

		final CostFunction<Staticproperties, Staticproperties> UserChosenCost = DistCostFunction;

		if (ndims == 2) {

			System.out.println("Appliying Median filter to the image");

			final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(img, 1);
			medfilter.process();
			final RandomAccessibleInterval<FloatType> preprocessedimage = medfilter.getResult();
			Normalize.normalize(Views.iterable(preprocessedimage), minval, maxval);
			ImageJFunctions.show(preprocessedimage);

			System.out.println("Median filter sucessfully applied");

			RandomAccessibleInterval<FloatType> currentframe0 = img;

			final RandomAccessibleInterval<FloatType> currentframepre0 = preprocessedimage;
			final Img<UnsignedByteType> newimg;
			try {
				new ImageJ();
				ImageJFunctions.wrap(currentframepre0, "curr");
				final ImagePlus currentimp = IJ.getImage();
				IJ.run("8-bit");

				newimg = ImagePlusAdapter.wrapByte(currentimp);
			} catch (final Exception e) {
				e.printStackTrace();
				return;
			}

			double bestdelta = GetDelta.Bestdeltaparam(newimg, delta, minSize, maxSize, maxVar, minDiversity,
					mindiameter, maxblobs, maxdelta, darktoBright);

			System.out.println("Making Component tree for " + "frame: " + 0);
			MserTree<UnsignedByteType> newtreeBtoD = MserTree.buildMserTree(newimg,
					new UnsignedByteType((int) bestdelta), minSize, maxSize, maxVar, minDiversity, false);
			System.out.println("Component tree made for " + "frame : " + 0);

			ImageJFunctions.show(currentframe0);
			final ImagePlus currentimp = IJ.getImage();
			final GetMSERtree<UnsignedByteType> visualizetree = new GetMSERtree<UnsignedByteType>(currentimp);
			final ArrayList<double[]> ellipselist = visualizetree.Roiarraylist(newtreeBtoD);
			visualizetree.visualise(ellipselist, Color.green);
			System.out.println("Visualization done");

		}

		if (ndims > 2) {
			final int maxframe = (int) img.dimension(ndims - 1);
			ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();

			for (int i = 0; i < maxframe; ++i) {

				IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);

				System.out.println("Appliying Median filter to current image");

				final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(currentframe, 1);
				medfilter.process();
				RandomAccessibleInterval<FloatType> currentframepre = medfilter.getResult();
				Normalize.normalize(Views.iterable(currentframepre), minval, maxval);

				System.out.println("Median filter sucessfully applied");

				final Img<UnsignedByteType> newimg;

				 ImageJFunctions.show(currentframepre);
				try {
					new ImageJ();

					final ImagePlus currentimp = IJ.getImage();
					IJ.run("8-bit");

					newimg = ImagePlusAdapter.wrapByte(currentimp);
				} catch (final Exception e) {
					e.printStackTrace();
					return;
				}

				System.out.println("Choosing best delta:");
				double bestdelta = GetDelta.Bestdeltaparam(newimg, delta, minSize, maxSize, maxVar, minDiversity,
						mindiameter, maxblobs, maxdelta, darktoBright);
				System.out.println("Making Component tree for " + "frame: " + i);
				MserTree<UnsignedByteType> newtreeBtoD = MserTree.buildMserTree(newimg,
						new UnsignedByteType((int) bestdelta), minSize, maxSize, maxVar, minDiversity, false);
				System.out.println("Component tree made for " + "frame : " + i);

				final ImagePlus currentimp = IJ.getImage();
				final GetMSERtree<UnsignedByteType> visualizetree = new GetMSERtree<UnsignedByteType>(currentimp);
				final ArrayList<double[]> ellipselist = visualizetree.Roiarraylist(newtreeBtoD);

				visualizetree.visualise(ellipselist, Color.green);
				System.out.println("Visualization done");

				final Float threshold = GlobalThresholding.AutomaticThresholding(currentframepre);
				Float val = new Float(threshold);
				final Img<BitType> bitimg = new ArrayImgFactory<BitType>().create(currentframe, new BitType());
				GetLocalmaxmin.ThresholdingBit(currentframepre, bitimg, val);

				// To get Blobs via LM fit and Gaussian detection, comment the
				// two
				// lines below if using DoG detection
				ArrayList<Staticproperties> Spotmaxbase = Makebloblist.returnRefinedBloblist(currentframe,
						currentframepre, bitimg, i);

				Allspots.add(Spotmaxbase);
				System.out.println("Finding blobs in Frame: " + i);
				System.out.println("Total number of Blobs found: " + Spotmaxbase.size());

			}

			SimpleMultiThreading.threadHaltUnClean();
			// Create an object for Kalman Filter tracking
			// Initial search radius as maximal distance allowed for initial
			// search
			// to initiate the Kalman Filter tracks
			final int initialSearchradius = 20;
			// For linking costs, this is how far we allow for the blob to move
			final int maxSearchradius = 20;
			final int missedframes = 20;

			KFSearchRefined KFsimple = new KFSearchRefined(Allspots, UserChosenCost, initialSearchradius,
					maxSearchradius, (int) img.dimension(ndims - 1), missedframes);
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
			DisplayBlobs.DisplayRefineddetection(detimg, frameandblob, minval, maxval);

		}
	}
}
