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
	import net.imglib2.img.array.ArrayImgFactory;
	import net.imglib2.img.display.imagej.ImageJFunctions;
	import net.imglib2.type.numeric.real.FloatType;
	import net.imglib2.view.IntervalView;
	import net.imglib2.view.Views;
	import overlaytrack.DisplayGraph;
	import preProcessingTools.Kernels;
	import preProcessingTools.MedianFilter2D;
	import segmentBlobs.Staticproperties;
	import trackerType.NNsearch;
	
	public class TrackblobsNNsearchDoG {
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

			for (int i = 0; i < maxframe; ++i) {
				IntervalView<FloatType> currentframe = Views.hyperSlice(img, ndims - 1, i);

				final MedianFilter2D<FloatType> medfilter = new MedianFilter2D<FloatType>(currentframe, 1);
				medfilter.process();
				final RandomAccessibleInterval<FloatType> inputimg = medfilter.getResult();
				RandomAccessibleInterval<FloatType> preprocessedimg = Kernels.Supressthresh(inputimg);
				Normalize.normalize(Views.iterable(preprocessedimg), minval, maxval);

				RandomAccessibleInterval<FloatType> currentframepre = preprocessedimg;

				// To get Blobs via DoG detection, uncomment these lines
				
				  final int minDiameter = 4; 
				  final int maxDiameter= 50; 
				  final double[] calibration = { imp.getCalibration().pixelWidth,
				  imp.getCalibration().pixelHeight, imp.getCalibration().pixelDepth}; 
				  ArrayList<Staticproperties> Spotmaxbase =
				  Makebloblist.returnBloblist(currentframe, currentframepre,
				  minDiameter, maxDiameter, calibration, i);
				 

				

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


