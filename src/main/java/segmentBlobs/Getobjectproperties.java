package segmentBlobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.tools.javac.util.Pair;

import blobObjects.Objprop;
import gaussianFits.GaussianPointfitter;
import ij.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;
import util.ImgLib2Util;

@SuppressWarnings("deprecation")
public class Getobjectproperties {

	// This class computes the min and max bounds of a labelled space and
	// returns the maxextent between these co-ordinates

	private  RandomAccessibleInterval<FloatType> inputimg;
	private  RandomAccessibleInterval<IntType> labelledimg;
	private  int ndims;
	private  int minRadius;
	private  int maxRadius;

	public Getobjectproperties(final RandomAccessibleInterval<FloatType> inputimg,
			final RandomAccessibleInterval<IntType> labelledimg, final int minRadius, final int maxRadius) {
		this.inputimg = inputimg;
		this.labelledimg = labelledimg;
		this.ndims = inputimg.numDimensions();
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;

	}
	
	public Getobjectproperties(final RandomAccessibleInterval<FloatType> inputimg,
			final RandomAccessibleInterval<IntType> labelledimg){
		
		this.inputimg = inputimg;
		this.labelledimg = labelledimg;
		this.ndims = inputimg.numDimensions();
		
	}

	// Once we have the label we get bounding box (BB for each of the labels, we
	// can only choose to get the BB for the largest region
	// after neglecting the background which carries the label 0
	public Objprop Getobjectprops(int currentlabel) {

		double Radius = 0;
		double totalintensity = 0;

		Point pos = GetLocalmaxmin.computeMaxinLabel(inputimg, labelledimg, currentlabel);

		Pair<Integer, Double> pair = EstimatedRadius(pos, minRadius, maxRadius);

		Radius = pair.fst;
		totalintensity = pair.snd;

		// Store all object properties in the java object and arraylist of
		// that object
		final Objprop props = new Objprop(currentlabel, 2 * Radius, totalintensity);

		return props;

	}
	
	
	public Objprop GetRefinedobjectprops(int currentlabel) throws Exception {
		
		Point pos = GetLocalmaxmin.computeMaxinLabel(inputimg, labelledimg, currentlabel);
		
		 
		
		
		GaussianPointfitter  MTlength = new GaussianPointfitter(inputimg, labelledimg, currentlabel);
		
		double[] final_param  = MTlength.Getfinalparam(pos);
		
        final double[] location = {final_param[1], final_param[2]};
		final double[] sigma = {1.0 / Math.sqrt(final_param[3]), 1.0 / Math.sqrt(final_param[4])};
		final double corr = final_param[5];
		final double totalintensity = final_param[0];
		final double diameter = 0.5 * (sigma[0] + sigma[1]);
		final double noise = final_param[6];
		final Objprop props = new Objprop(currentlabel,diameter, location, sigma, corr, noise, totalintensity);
		return props;
		
		
	}

	public Objprop Improveobjectprops(RealLocalizable realpos, int currentlabel) {

		double Radius = 0;
		double totalintensity = 0;

		Pair<Double, Double> pair = ImproveRadius(realpos, minRadius, maxRadius);

		Radius = pair.fst;
		totalintensity = pair.snd;

		// Store all object properties in the java object and arraylist of
		// that object
		final Objprop props = new Objprop(currentlabel, 2 * Radius, totalintensity);

		return props;
	}

	public Pair<Integer, Double> EstimatedRadius(Localizable point, int minRadius, int maxRadius) {

		int BlobRadius = 0;
		double Blobintensity = 0;
		double maxdiff = Double.MIN_VALUE;

		int actualmaxRadius = maxRadius;

		int userRadius = maxRadius - minRadius;

		/*
		 * Here we correct for too big inputed maxDiamter. The routine below
		 * tries to estimate the size of the blob by fitting rings of increasing
		 * diameter and chooses the diamter for which the total intensity
		 * difference is maximum.
		 * 
		 */
		for (int Radius = 0; Radius < userRadius; ++Radius) {

			if (point.getDoublePosition(0) + Radius + minRadius >= inputimg.dimension(0)
					|| point.getDoublePosition(1) + Radius + minRadius >= inputimg.dimension(1)
					|| point.getDoublePosition(0) - Radius - minRadius <= 0
					|| point.getDoublePosition(1) - Radius - minRadius <= 0) {
				actualmaxRadius = minRadius + Radius;

				break;
			}

		}

		int nRadius = actualmaxRadius - minRadius;
		final double[] totalintensity = new double[nRadius];
		final double[] meanintensity = new double[nRadius];
		final double[] totalarea = new double[nRadius];

		for (int Radius = 0; Radius < nRadius; ++Radius) {

			HyperSphere<FloatType> sphere = new HyperSphere<FloatType>(inputimg, point, Radius + minRadius);

			HyperSphereCursor<FloatType> sphereCursor = sphere.localizingCursor();

			while (sphereCursor.hasNext()) {

				sphereCursor.fwd();

				totalarea[Radius]++;

				final RealSum realSumA = new RealSum();

				final FloatType type = sphereCursor.get();

				realSumA.add(type.getRealDouble());

				totalintensity[Radius] = realSumA.getSum();
				meanintensity[Radius] = totalintensity[Radius] / totalarea[Radius];

			}

		}

		for (int Radius = 0; Radius < nRadius - 1; ++Radius) {

			if (meanintensity[Radius] - meanintensity[Radius + 1] > maxdiff) {

				maxdiff = meanintensity[Radius] - meanintensity[Radius + 1];

				BlobRadius = Radius + minRadius;

				Blobintensity = totalintensity[Radius];
			}
		}

		Pair<Integer, Double> pair = new Pair<Integer, Double>(BlobRadius, Blobintensity);

		return pair;
	}

	public Pair<Double, Double> ImproveRadius(RealLocalizable point, int minRadius, int maxRadius) {

		double BlobRadius = 0, BlobRadiuspre = 0, BlobRadiuspost = 0;
		double Blobintensity = 0, Blobintensitypre = 0, Blobintensitypost = 0;
		double maxdiff = Double.MIN_VALUE;

		int actualmaxRadius = maxRadius;

		int userRadius = maxRadius - minRadius;

		/*
		 * Here we correct for too big inputed maxDiamter. The routine below
		 * tries to estimate the size of the blob by fitting rings of increasing
		 * diameter and chooses the diamter for which the total intensity
		 * difference is maximum.
		 * 
		 */
		for (int Radius = 0; Radius < userRadius; ++Radius) {

			if (point.getDoublePosition(0) + Radius + minRadius >= inputimg.dimension(0)
					|| point.getDoublePosition(1) + Radius + minRadius >= inputimg.dimension(1)
					|| point.getDoublePosition(0) - Radius - minRadius <= 0
					|| point.getDoublePosition(1) - Radius - minRadius <= 0) {
				actualmaxRadius = minRadius + Radius;

				break;
			}

		}
		Point floatpoint = new Point(ndims);
		for (int d = 0; d < ndims; ++d) {

			floatpoint.setPosition((int) point.getDoublePosition(d), d);

		}

		int nRadius = actualmaxRadius - minRadius;
		final double[] totalintensity = new double[nRadius];
		final double[] meanintensity = new double[nRadius];
		final double[] totalarea = new double[nRadius];

		for (int Radius = 0; Radius < nRadius; ++Radius) {

			HyperSphere<FloatType> sphere = new HyperSphere<FloatType>(inputimg, floatpoint, Radius + minRadius);

			HyperSphereCursor<FloatType> sphereCursor = sphere.localizingCursor();

			while (sphereCursor.hasNext()) {

				sphereCursor.fwd();

				totalarea[Radius]++;

				final RealSum realSumA = new RealSum();

				final FloatType type = sphereCursor.get();

				realSumA.add(type.getRealDouble());

				totalintensity[Radius] = realSumA.getSum();
				meanintensity[Radius] = totalintensity[Radius] / totalarea[Radius];

			}

		}

		for (int Radius = 1; Radius < nRadius - 2; ++Radius) {

			if (meanintensity[Radius] - meanintensity[Radius + 1] > maxdiff) {

				maxdiff = meanintensity[Radius] - meanintensity[Radius + 1];

				BlobRadius = Radius + minRadius;

				Blobintensity = totalintensity[Radius];

				BlobRadiuspre = Radius + minRadius - 1;

				Blobintensitypre = totalintensity[Radius - 1];

				BlobRadiuspost = Radius + minRadius + 1;

				Blobintensitypost = totalintensity[Radius + 1];
			}
		}

		double Bestradius;

		if (1 > BlobRadius || totalintensity.length - 1 == BlobRadius) {

			Bestradius = BlobRadius;
		}

		else {

			Bestradius = quadratic1DInterpolation(BlobRadiuspre, Blobintensitypre, BlobRadius, Blobintensity,
					BlobRadiuspost, Blobintensitypost);
		}

		Pair<Double, Double> pair = new Pair<Double, Double>(Bestradius, Blobintensity);

		return pair;
	}

	private static final double quadratic1DInterpolation(final double x1, final double y1, final double x2,
			final double y2, final double x3, final double y3) {
		final double d2 = 2 * ((y3 - y2) / (x3 - x2) - (y2 - y1) / (x2 - x1)) / (x3 - x1);
		if (d2 == 0)
			return x2;
		else {
			final double d1 = (y3 - y2) / (x3 - x2) - d2 / 2 * (x3 - x2);
			return x2 - d1 / d2;
		}
	}

	public double Distance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;
		final double ndims = cordone.length;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return Math.sqrt(distance);
	}
}