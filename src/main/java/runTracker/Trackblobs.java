package runTracker;

import java.io.File;
import java.util.ArrayList;

import ij.ImageJ;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Trackblobs {

	public static void main(String[] args) {

		new ImageJ();

		// Load the stack of images
		final Img<FloatType> img = util.ImgLib2Util.openAs32Bit(new File("src/main/resources/5framestack.tif"),
				new ArrayImgFactory<FloatType>());
		int ndims = img.numDimensions();
		
		// Display stack
		ImageJFunctions.show(img);

		final double[] estimatedradius = { 15, 16 };

		for (int i = 1; i < img.dimension(ndims - 1) ; ++i) {
			IntervalView<FloatType> baseframe = Views.hyperSlice(img, ndims - 1, i - 1);
			IntervalView<FloatType> targetframe = Views.hyperSlice(img, ndims - 1, i);
			
			System.out.println("Starting nearest neighbour tracking in :" + " Frame: " + (i - 1) + " and " + i  );
			nearestNeighbour.NearestNeighboursearch.NearestNeighbour(baseframe, targetframe, estimatedradius);
		}

	}
}
