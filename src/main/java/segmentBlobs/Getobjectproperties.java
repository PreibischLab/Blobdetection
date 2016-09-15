package segmentBlobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.tools.javac.util.Pair;

import ij.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
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
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;

import util.ImgLib2Util;

@SuppressWarnings("deprecation")
public class Getobjectproperties {

	// This class computes the min and max bounds of a labelled space and
	// returns the maxextent between these co-ordinates

	private final RandomAccessibleInterval<FloatType> inputimg;
	private final RandomAccessibleInterval<IntType> labelledimg;
	private final int ndims;
	private final int minRadius;
	private final int maxRadius;
	
	
	public Getobjectproperties(final RandomAccessibleInterval<FloatType> inputimg, 
			final RandomAccessibleInterval<IntType> labelledimg, final int minRadius, final int maxRadius){
		this.inputimg = inputimg;
		this.labelledimg = labelledimg;
		this.ndims = inputimg.numDimensions();
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		
	}
	

	// Once we have the label we get bounding box (BB for each of the labels, we
	// can only choose to get the BB for the largest region
	// after neglecting the background which carries the label 0
	public  Objprop Getobjectprops( int currentlabel) {

		Cursor<IntType> intCursor = Views.iterable(labelledimg).localizingCursor();
		RandomAccess<FloatType> ranac = inputimg.randomAccess();

		double[] position = new double[ndims];
		double[] maxIntensityposition = new double[ndims];
		// Go through the whole image and add every pixel, that belongs to
			// the currently processed label

			double[] minVal = { Double.MAX_VALUE, Double.MAX_VALUE };
			double[] maxVal = { -Double.MIN_VALUE, -Double.MIN_VALUE };
			double maxIntensity = -Double.MIN_VALUE;
			double area = 0;
			double Radius = 0;
			double totalintensity = 0;
			
					
					
					maxIntensity = GetLocalmaxmin.computeMaxIntensityinlabel(inputimg, labelledimg, currentlabel);
					Point pos = GetLocalmaxmin.computeMaxinLabel(inputimg,labelledimg,currentlabel);
						
						Pair<Integer, Double> pair = EstimatedRadius(pos, minRadius, maxRadius);
						Radius = pair.fst;
						totalintensity = pair.snd;
						area = Math.PI * Radius * Radius ;
				
				

			
			
			// Store all object properties in the java object and arraylist of
			// that object
			final Objprop props = new Objprop(currentlabel, 2 * Radius , area, totalintensity);
			

			return props;

		

	}
	
	public Pair<Integer, Double> EstimatedRadius(Localizable point, int minRadius, int maxRadius){
		
		int BlobRadius = 0;
		double Blobintensity = 0;
		double maxdiff = -Double.MAX_VALUE;
		int nRadius = maxRadius - minRadius;
		
		final double[] totalintensity = new double[nRadius];
		
		for (int Radius = 0; Radius < nRadius; ++Radius){
			
			HyperSphere<FloatType> sphere = new HyperSphere<FloatType>(inputimg, point, Radius + minRadius);
			
			HyperSphereCursor<FloatType> sphereCursor = sphere.localizingCursor();
			
			while(sphereCursor.hasNext()){
				
				sphereCursor.fwd();
				
				final RealSum realSumA = new RealSum();
				

				final FloatType type = sphereCursor.get();
				
					realSumA.add(type.getRealDouble());
				
				
				 totalintensity[Radius] = realSumA.getSum();
				
			}
			
			
		}
		
		for (int Radius = 0; Radius < nRadius - 1; ++Radius){
			
			if (totalintensity[Radius] - totalintensity[Radius + 1] > maxdiff){
				
				maxdiff = totalintensity[Radius] - totalintensity[Radius + 1];
				
				BlobRadius = Radius + minRadius;
				
				Blobintensity = totalintensity[Radius];
			}
		}
		
		Pair<Integer, Double> pair = new Pair<Integer, Double>(BlobRadius, Blobintensity);
		
		return pair;
	}
	
	public  double Distance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;
		final double ndims = cordone.length;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return Math.sqrt(distance);
	}
}
