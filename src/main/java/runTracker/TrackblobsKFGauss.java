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
			//	new File(
				//					"/Users/varunkapoor/Downloads/test.jpg"),
				//			new ArrayImgFactory<FloatType>());
				
				
			//		new File(
			//			"/Users/varunkapoor/Documents/Will_data/20160709_cy5seeds_cy3tub_Xeno6uM-duplicate-shorter.tif"),
			//	new ArrayImgFactory<FloatType>());
				
				new File(
						"/Users/varunkapoor/Documents/Pierre_data/Recording_Cell_Culture_Kapoor/mCherry_ShortET-secdup-test.tif"),
				new ArrayImgFactory<FloatType>());
		
		
		int ndims = img.numDimensions();
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(img), minval, maxval);
		
		ImagePlus imp = ImageJFunctions.show(img);
		ImagePlus impcopy = ImageJFunctions.show(img);
		final int delta = 10;
		final long minSize = 10;
		final long maxSize = Long.MAX_VALUE;
		final double maxVar = 0.2;
		final double minDiversity = 0;
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
       
		
		if (ndims == 2){
			
			System.out.println("Appliying Median filter to the image");
			
			final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(img, 1);
			medfilter.process();
			final RandomAccessibleInterval<FloatType> preprocessedimage = medfilter.getResult();
			Normalize.normalize(Views.iterable(preprocessedimage), minval, maxval);
			ImageJFunctions.show(preprocessedimage);
			
			System.out.println("Median filter sucessfully applied");
			
			RandomAccessibleInterval<FloatType> currentframe0 = img;
			
			final RandomAccessibleInterval<FloatType> currentframepre0 =  preprocessedimage;
			ImageJFunctions.show(currentframepre0);
			final Img< UnsignedByteType > newimg;
			try
			{
				new ImageJ();
				
				
				final ImagePlus currentimp = IJ.getImage();
				IJ.run("8-bit");
				
				
				
				newimg = ImagePlusAdapter.wrapByte( currentimp );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
				return;
			}
			
			
			
			System.out.println("Making Component tree for " + "frame: " + 0);
			MserTree<UnsignedByteType> newtreeBtoD = MserTree.buildMserTree( newimg, new UnsignedByteType( delta ), minSize, maxSize, maxVar, minDiversity, false );
			System.out.println("Component tree made for " + "frame : " + 0);
			
			ImageJFunctions.show(currentframe0);
			final ImagePlus currentimp = IJ.getImage();
			final GetMSERtree<UnsignedByteType> visualizetree = new GetMSERtree<UnsignedByteType>(currentimp);
			visualizetree.visualise(newtreeBtoD, Color.green);
		   System.out.println("Visualization done");
			
		
			
			
		}
		
		
		if (ndims > 2){
		final int maxframe = (int) img.dimension(ndims - 1);
		ArrayList<ArrayList<Staticproperties>> Allspots = new ArrayList<ArrayList<Staticproperties>>();
		
		System.out.println("Appliying Median filter to the stack");
		
		final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(img, 1);
		medfilter.process();
		final RandomAccessibleInterval<FloatType> preprocessedimage = medfilter.getResult();
		Normalize.normalize(Views.iterable(preprocessedimage), minval, maxval);
		ImageJFunctions.show(preprocessedimage);
		
		System.out.println("Median filter sucessfully applied");
		
		IntervalView<FloatType> currentframe0 = Views.hyperSlice(img, ndims - 1, 0);
		
		final RandomAccessibleInterval<FloatType> currentframepre0 =  Views.hyperSlice(preprocessedimage, ndims - 1, 0);
		
		
		ImageJFunctions.show(currentframepre0);
		final Img< UnsignedByteType > newimg;
		try
		{
			new ImageJ();
			
			
			final ImagePlus currentimp = IJ.getImage();
			IJ.run("8-bit");
			
			
			
			newimg = ImagePlusAdapter.wrapByte( currentimp );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			return;
		}
		
		
		
		System.out.println("Making Component tree for " + "frame: " + 0);
		MserTree<UnsignedByteType> newtreeBtoD = MserTree.buildMserTree( newimg, new UnsignedByteType( delta ), minSize, maxSize, maxVar, minDiversity, false );
		System.out.println("Component tree made for " + "frame : " + 0);
	
    Iterator<Mser<UnsignedByteType>> listiterator = newtreeBtoD.iterator();
    
    listiterator.next().getChildren();
		ImageJFunctions.show(currentframe0);
		final ImagePlus currentimp = IJ.getImage();
		final GetMSERtree<UnsignedByteType> visualizetree = new GetMSERtree<UnsignedByteType>(currentimp);
		
		
		visualizetree.visualise(newtreeBtoD, Color.green);
		System.out.println("Visualization done");
	
		//SortedSet<Double> MseratT = GetMSERtree.MseratThreshold(newtreeBtoD);
		//System.out.println(MseratT);
		SimpleMultiThreading.threadHaltUnClean();
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
}
