package runTracker;

import java.awt.Color;
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
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import mserTree.GetMSERtree;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
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
				new File(
						"../res/5framestack.tif"),
				new ArrayImgFactory<FloatType>());
		
		
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		
		ImagePlus imp = ImageJFunctions.show(img);
		ImagePlus impcopy = ImageJFunctions.show(img);
		final double delta = 10;
		final long minSize = 2;
		final long maxSize = 4000;
		final double maxVar = 0.8;
		final double minDiversity = 0.8;
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
			
			
			final RandomAccessibleInterval<UnsignedByteType> mserInput = Converters.convert(
					 currentframepre,
					 new Converter<FloatType, UnsignedByteType>(){

						@Override
						
						
						
						public void convert(FloatType input, UnsignedByteType output) {
							output.setReal(input.getRealDouble() * 1000);
							
						}
						 
					 },
					 new UnsignedByteType());
			
			
			
			ImageJFunctions.show(mserInput).setTitle("MserInput");
			
			final ImageStack stack = new ImageStack( (int) currentframe.dimension( 0 ), (int) currentframe.dimension( 1 ) );
			
			ImagePlus currentimp = IJ.getImage();
			
			System.out.println("Making Component tree for " + "frame: " + i);
			MserTree<UnsignedByteType> newtree =  MserTree.buildMserTree(mserInput,  delta, minSize, maxSize, maxVar, minDiversity, true);
			System.out.println("Component tree makde for " + "frame : " + i);
			
			final GetMSERtree visualizetree = new GetMSERtree(currentimp, stack);
			visualizetree.visualise(newtree, Color.green);
			
			final ImagePlus imptmp = new ImagePlus("components", stack);
			imptmp.show();
			
			
			final Float threshold = GlobalThresholding.AutomaticThresholding(currentframepre);
			Float val = new Float(threshold);
			final Img<BitType> bitimg = new ArrayImgFactory<BitType>().create(currentframe, new BitType());
			GetLocalmaxmin.ThresholdingBit(currentframepre, bitimg, val);

			// To get Blobs via LM fit and Gaussian detection, comment the two
			// lines below if using DoG detection
			ArrayList<Staticproperties> Spotmaxbase = Makebloblist.returnRefinedBloblist(currentframe, currentframepre, bitimg,
					i);

			Allspots.add(Spotmaxbase);
			System.out.println("Finding blobs in Frame: " + i);
			System.out.println("Total number of Blobs found: " + Spotmaxbase.size());
		}

		// Create an object for Kalman Filter tracking
		// Initial search radius as maximal distance allowed for initial search
		// to initiate the Kalman Filter tracks
		final int initialSearchradius = 20;
		// For linking costs, this is how far we allow for the blob to move
		final int maxSearchradius = 20;
		final int missedframes = 20;

		KFSearchRefined KFsimple = new KFSearchRefined(Allspots, UserChosenCost, initialSearchradius, maxSearchradius,
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
		DisplayBlobs.DisplayRefineddetection(detimg, frameandblob, minval, maxval);

		ImageJFunctions.show(detimg);
	}
}
