package segmentBlobs;

import java.util.ArrayList;

import blobObjects.Objprop;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Segmentbywatershed {

	public static RandomAccessibleInterval<IntType> getsegmentedimage(
			final RandomAccessibleInterval<FloatType> blobimage, boolean softThreshold) {

		RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage,
				new IntType());

		labelledimage = segmentBlobs.Watersheddding.Dowatersheddingonly(blobimage, softThreshold);

		// ImageJFunctions.show(labelledimage);
		return labelledimage;
	}

	public static ArrayList<RefinedPeak<Point>> DoGdetection(final IntervalView<FloatType> blobimage,
			final RandomAccessibleInterval<IntType> labelledimage, final int estimatedDiameter,
			final double[] calibration, boolean softThreshold) {
		final int ndims = blobimage.numDimensions();

		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(ndims);

	


		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);

		// Background is lablled as 0 so start from 1 to Maxlabel - 1

		for (int label = 1; label < Maxlabel - 1; ++label) {
			
			
			
			RandomAccessibleInterval<FloatType> outimg  = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
			final long[] minCorner = Watersheddding.GetMincorners(labelledimage, label);
			final long[] maxCorner = Watersheddding.GetMaxcorners(labelledimage, label);
			FinalInterval smallinterval = new FinalInterval(minCorner , maxCorner );
			
			
			double sigma1 = estimatedDiameter / (2 * Math.sqrt( smallinterval.numDimensions() )) * 0.9;
			double sigma2 = estimatedDiameter / (2 * Math.sqrt( smallinterval.numDimensions() )) * 1.1;
			
			// Determine local threshold value for each label
			Float val = GlobalThresholding.AutomaticThresholding(outimg);
			Float threshold = new Float(val);

			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendMirrorSingle(outimg), smallinterval,
					new double[] { calibration[0], calibration[1] }, sigma1, sigma2, DogDetection.ExtremaType.MINIMA,
					threshold, true);

			// Detect minima in Scale space
			for (int index = 0; index < newdog.getSubpixelPeaks().size() ; ++index){
				
				SubpixelMinlist.add(newdog.getSubpixelPeaks().get(index));
				//System.out.println(label + " " + newdog.getSubpixelPeaks().get(index));
			}
		
		
			
		}
			
		
		return SubpixelMinlist;
		
	}
	
	/*
			final Objprop objproperties = props.Getobjectprops(label);

			if (objproperties != null){
			final double estimatedDiameter = objproperties.diameter;

		//	System.out.println(label + " " + estimatedDiameter );
			
			double sigma1 =  estimatedDiameter / (1 + Math.sqrt(2));
			double sigma2 = sigma1 * Math.sqrt(2);

			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendMirrorSingle(outimg), range,
					new double[] { calibration[0], calibration[1] }, sigma1, sigma2, DogDetection.ExtremaType.MINIMA,
					threshold, true);

			// Detect minima in Scale space
			SubpixelMinlist = newdog.getSubpixelPeaks();
			
			for (int index = 0; index < SubpixelMinlist.size(); ++index) {

				final Staticproperties statprops = new Staticproperties(objproperties.Label, framenumber,
						objproperties.diameter,
						new double[] { SubpixelMinlist.get(index).getDoublePosition(0),
								SubpixelMinlist.get(index).getDoublePosition(1) },
						objproperties.totalintensity, GetLocalmaxmin.computeMaxIntensity(blobimage),
						GetLocalmaxmin.computeMinIntensity(blobimage));

				 
				
				staticprops.add(statprops);

			
		}
		}
		}
		return staticprops;
	}
*/
}
