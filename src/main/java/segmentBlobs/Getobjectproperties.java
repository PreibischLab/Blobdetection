package segmentBlobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ij.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;

import util.ImgLib2Util;

@SuppressWarnings("deprecation")
public class Getobjectproperties {

	// This class computes the min and max bounds of a labelled space and
	// returns the maxextent between these co-ordinates

	private final RandomAccessibleInterval<FloatType> inputimg;
	private final RandomAccessibleInterval<IntType> seedimg;
	private final int ndims;

	
	public Getobjectproperties(final RandomAccessibleInterval<FloatType> inputimg, RandomAccessibleInterval<IntType> seedimg){
		this.inputimg = inputimg;
		this.seedimg = seedimg;
		this.ndims = inputimg.numDimensions();
		
		
	}
	// This is the connected component bit, all the objects that are connected
	// in the image are given a unique label
	public NativeImgLabeling<Integer, IntType> PrepareSeedImage() {

		// Preparing the seed image
		RandomAccessibleInterval<BitType> maximgBit = new ArrayImgFactory<BitType>().create(inputimg, new BitType());
		final Float threshold = GlobalThresholding.AutomaticThresholding(inputimg);
		GetLocalmaxmin.ThresholdingBit(inputimg, maximgBit, threshold);

		// Old Labeling type
		final NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		// The label generator, I label the background to be 0
		final Iterator<Integer> labelGenerator = AllConnectedComponents.getIntegerNames(0);

		// Getting unique labelled image (old version)
		AllConnectedComponents.labelAllConnectedComponents(oldseedLabeling, maximgBit, labelGenerator,
				AllConnectedComponents.getStructuringElement(inputimg.numDimensions()));

		return oldseedLabeling;
	}

	// Once we have the label we get bounding box (BB for each of the labels, we
	// can only choose to get the BB for the largest region
	// after neglecting the background which carries the label 0
	public  Objprop Getobjectprops( int currentlabel) {

		Cursor<IntType> intCursor = Views.iterable(seedimg).localizingCursor();
		RandomAccess<FloatType> ranac = inputimg.randomAccess();

		long[] position = new long[ndims];

		// Go through the whole image and add every pixel, that belongs to
			// the currently processed label

			double[] minVal = { Double.MAX_VALUE, Double.MAX_VALUE };
			double[] maxVal = { Double.MIN_VALUE, Double.MIN_VALUE };
			int area = 0;
			double totalintensity = 0;
			while (intCursor.hasNext()) {
				intCursor.fwd();

				int i = intCursor.get().get();
				// Now we are going inside a labelled space
				if (i == currentlabel) {
					area++;
					

					ranac.setPosition(intCursor);
					ranac.localize(position);
					
					// Here we compute the bounding box for each labelled space
					for (int d = 0; d < ndims; ++d) {
						if (position[d] < minVal[d]) {
							minVal[d] = position[d];
						}
						if (position[d] > maxVal[d]) {
							maxVal[d] = position[d];
						}

					}
					
					final RealSum realSumA = new RealSum();
					

					final FloatType type = ranac.get();
					
						realSumA.add(type.getRealDouble());
					
					
					 totalintensity = realSumA.getSum() / area;

				}

			}
			// Get the maxextent between min and max co-ordinates of the box
			// computed

			double diameter = 0;
			for (int d = 0; d < ndims; ++d)
				diameter += (maxVal[d] - minVal[d]) * (maxVal[d] - minVal[d]);

			// Store all object properties in the java object and arraylist of
			// that object
			final Objprop props = new Objprop(currentlabel, Math.sqrt(diameter), area, totalintensity,  minVal, maxVal);
			

			return props;

		

	}

}
