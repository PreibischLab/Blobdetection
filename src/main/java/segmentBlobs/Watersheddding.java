package segmentBlobs;

import java.util.Iterator;

import com.sun.tools.javac.util.Pair;

import net.imglib2.Cursor;
import net.imglib2.KDTree;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labeling.DefaultROIStrategyFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingROIStrategy;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import segmentBlobs.GetLocalmaxmin;
import segmentBlobs.GlobalThresholding;

@SuppressWarnings("deprecation")
public class Watersheddding {

	public static enum InverseType {
		Straight, Inverse
	}

	public static RandomAccessibleInterval<IntType> Dowatersheddingonly(
			final RandomAccessibleInterval<FloatType> biginputimg, final RandomAccessibleInterval<BitType> bitimg) {

		// Perform the distance transform
		final Img<FloatType> distimg = new ArrayImgFactory<FloatType>().create(biginputimg, new FloatType());

		DistanceTransformImage(biginputimg, distimg, bitimg, InverseType.Straight);

		// Prepare seed image for watershedding
		NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(biginputimg, new IntType()));

		oldseedLabeling = PrepareSeedImage(biginputimg, bitimg);

		// Do watershedding on the distance transformed image

		NativeImgLabeling<Integer, IntType> outputLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(biginputimg, new IntType()));

		outputLabeling = GetlabeledImage(distimg, oldseedLabeling);
	
		return outputLabeling.getStorageImg();
	}


	
	
	public static void  DistanceTransformImage(RandomAccessibleInterval<FloatType> inputimg,
			RandomAccessibleInterval<FloatType> outimg,RandomAccessibleInterval<BitType> bitimg, final InverseType invtype) {
		int n = inputimg.numDimensions();

		// make an empty list
		final RealPointSampleList<BitType> list = new RealPointSampleList<BitType>(n);

		
		
		// cursor on the binary image
		final Cursor<BitType> cursor = Views.iterable(bitimg).localizingCursor();

		// for every pixel that is 1, make a new RealPoint at that location
		while (cursor.hasNext())
			if (cursor.next().getInteger() == 1)
				list.add(new RealPoint(cursor), cursor.get());

		// build the KD-Tree from the list of points that == 1
		final KDTree<BitType> tree = new KDTree<BitType>(list);

		// Instantiate a nearest neighbor search on the tree (does not modifiy
		// the tree, just uses it)
		final NearestNeighborSearchOnKDTree<BitType> search = new NearestNeighborSearchOnKDTree<BitType>(tree);

		// randomaccess on the output
		final RandomAccess<FloatType> ranac = outimg.randomAccess();

		// reset cursor for the input (or make a new one)
		cursor.reset();

		// for every pixel of the binary image
		while (cursor.hasNext()) {
			cursor.fwd();

			// set the randomaccess to the same location
			ranac.setPosition(cursor);

			// if value == 0, look for the nearest 1-valued pixel
			if (cursor.get().getInteger() == 0) {
				// search the nearest 1 to the location of the cursor (the
				// current 0)
				search.search(cursor);

				// get the distance (the previous call could return that, this
				// for generality that it is two calls)
				switch (invtype) {

				case Straight:
					ranac.get().setReal(search.getDistance());
					break;
				case Inverse:
					ranac.get().setReal(-search.getDistance());
					break;

				}

			} else {
				// if value == 1, no need to search
				ranac.get().setZero();
			}
		}
	}

	public static NativeImgLabeling<Integer, IntType> PrepareSeedImage(RandomAccessibleInterval<FloatType> inputimg,
			RandomAccessibleInterval<BitType> maximgBit) {

		// Old Labeling type

		final NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		// The label generator for both new and old type
		final Iterator<Integer> labelGenerator = AllConnectedComponents.getIntegerNames(0);

		// Getting unique labelled image (old version)
		AllConnectedComponents.labelAllConnectedComponents(oldseedLabeling, maximgBit, labelGenerator,
				AllConnectedComponents.getStructuringElement(inputimg.numDimensions()));

		return oldseedLabeling;
	}

	public static long[] GetMaxcorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] maxVal = { Long.MIN_VALUE, Long.MIN_VALUE };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p > maxVal[d])
						maxVal[d] = p;

				}

			}
		}

		return maxVal;

	}

	public static long[] GetMincorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] minVal = { Long.MAX_VALUE, Long.MAX_VALUE };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p < minVal[d])
						minVal[d] = p;
				}

			}
		}

		return minVal;

	}

	public static Pair<long[], long[]> GetBoundingbox(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] position = new long[n];
		long[] minVal = { Long.MAX_VALUE, Long.MAX_VALUE };
		long[] maxVal = { Long.MIN_VALUE, Long.MIN_VALUE };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}

			}
		}

		Pair<long[], long[]> boundingBox = new Pair<long[], long[]>(minVal, maxVal);
		return boundingBox;
	}

	public static int GetMaxlabelsseeded(RandomAccessibleInterval<IntType> intimg) {

		// To get maximum Labels on the image
		Cursor<IntType> intCursor = Views.iterable(intimg).cursor();
		int currentLabel = 1;
		boolean anythingFound = true;
		while (anythingFound) {
			anythingFound = false;
			intCursor.reset();
			while (intCursor.hasNext()) {
				intCursor.fwd();
				int i = intCursor.get().get();
				if (i == currentLabel) {

					anythingFound = true;

				}
			}
			currentLabel++;
		}

		return currentLabel;

	}

	@SuppressWarnings("deprecation")
	public static NativeImgLabeling<Integer, IntType> GetlabeledImage(RandomAccessibleInterval<FloatType> inputimg,
			NativeImgLabeling<Integer, IntType> seedLabeling) {

		int n = inputimg.numDimensions();
		long[] dimensions = new long[n];

		for (int d = 0; d < n; ++d)
			dimensions[d] = inputimg.dimension(d);
		final NativeImgLabeling<Integer, IntType> outputLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		final Watershed<FloatType, Integer> watershed = new Watershed<FloatType, Integer>();

		watershed.setSeeds(seedLabeling);
		watershed.setIntensityImage(inputimg);
		watershed.setStructuringElement(AllConnectedComponents.getStructuringElement(2));
		watershed.setOutputLabeling(outputLabeling);
		watershed.process();
		DefaultROIStrategyFactory<Integer> deffactory = new DefaultROIStrategyFactory<Integer>();
		LabelingROIStrategy<Integer, Labeling<Integer>> factory = deffactory
				.createLabelingROIStrategy(watershed.getResult());
		outputLabeling.setLabelingCursorStrategy(factory);

		return outputLabeling;

	}

	public static RandomAccessibleInterval<FloatType> CurrentLabelImage(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {

		RandomAccess<FloatType> inputRA = originalimg.randomAccess();

		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();

		RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(originalimg,
				new FloatType());
		RandomAccess<FloatType> imageRA = outimg.randomAccess();

		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {

				imageRA.get().set(inputRA.get());

			}

		}

		return outimg;

	}

}
