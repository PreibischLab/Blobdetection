package segmentBlobs;

import java.util.ArrayList;

import com.googlecode.gentyref.GenericTypeReflector;

import blobObjects.Objprop;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Segmentbywatershed {

	public static RandomAccessibleInterval<IntType> getsegmentedimage(
			final RandomAccessibleInterval<FloatType> blobimage, final RandomAccessibleInterval<BitType> bitimg) {

		RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage,
				new IntType());

		labelledimage = segmentBlobs.Watersheddding.Dowatersheddingonly(blobimage, bitimg);

		return labelledimage;
	}
	
	
	public static ArrayList<Staticproperties> Gaussdetection(final IntervalView<FloatType> blobimage,
			final RandomAccessibleInterval<IntType> labelledimage,
			final RandomAccessibleInterval<BitType> bitimg,
			int framenumber) throws Exception{
		final int ndims = blobimage.numDimensions();
		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);
		ArrayList<Staticproperties> staticprops = new ArrayList<Staticproperties>(ndims);
		
		for (int label = 1; label < Maxlabel - 1; ++label) {
			RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(blobimage,
					new FloatType());
			outimg = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
		
			final Getobjectproperties props = new Getobjectproperties(outimg, labelledimage, bitimg);
		
			
		final Objprop Refinedobjectproperties = props.GetRefinedobjectprops(label);
			

		if (Math.abs(Refinedobjectproperties.corr) <= 1.0E-3)
			Refinedobjectproperties.corr = 0;
		final Staticproperties statprops = new Staticproperties(label, framenumber,
				Refinedobjectproperties.location,
				Refinedobjectproperties.sigma,
				Refinedobjectproperties.corr,
				Refinedobjectproperties.noise,
				Refinedobjectproperties.diameter,
				Refinedobjectproperties.totalintensity,
				Refinedobjectproperties.Circularity);

		final double radius = 0.5 * (Refinedobjectproperties.sigma[0]  + 
				               Refinedobjectproperties.sigma[1]);
		// System.out.println(label + " " + Refinedobjectproperties.totalintensity );
		if (Refinedobjectproperties.totalintensity > 0 && Refinedobjectproperties.sigma[0] > 0 
				&& Refinedobjectproperties.sigma[1] > 0 && Refinedobjectproperties.location[0] > 0
				&& Refinedobjectproperties.location[1] > 0 && radius > 6 ){
		//	System.out.println( "Location: " + "" + Refinedobjectproperties.location[0] + " " + Refinedobjectproperties.location[1]
		//n			+ "Circularity" + " " + Refinedobjectproperties.Circularity);
			
		staticprops.add(statprops);
		}
		}
		return staticprops;
	}
	

	public static ArrayList<Staticproperties> DoGdetection(final IntervalView<FloatType> blobimage,
			final RandomAccessibleInterval<IntType> labelledimage,
			final RandomAccessibleInterval<BitType> bitimg,
			final int minRadius, final int maxRadius,
			final double[] calibration, int framenumber) {
		final int ndims = blobimage.numDimensions();

		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(ndims);

		ArrayList<Staticproperties> staticprops = new ArrayList<Staticproperties>(ndims);

		final Getobjectproperties props = new Getobjectproperties(blobimage, labelledimage, bitimg, minRadius, maxRadius);

		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);

		// Background is lablled as 0 so start from 1 to Maxlabel - 1

		for (int label = 1; label < Maxlabel - 1; ++label) {
			RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(blobimage,
					new FloatType());
			

			outimg = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
			final long[] minCorner = Watersheddding.GetMincorners(labelledimage, label);
			final long[] maxCorner = Watersheddding.GetMaxcorners(labelledimage, label);
			FinalInterval smallinterval = new FinalInterval(minCorner , maxCorner );
			
			final Objprop objproperties = props.Getobjectprops(label);

			final double estimatedDiameter = objproperties.diameter;
			
			// Determine local threshold value for each label, choose low value such as 0.5 * val to include more peak detections
			Float val = GlobalThresholding.AutomaticThresholding(outimg);
			Float threshold = new Float(val);

			double sigma1 =  estimatedDiameter ;
			double sigma2 = 1.2 * estimatedDiameter ;

			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(outimg), smallinterval,
					new double[] { calibration[0], calibration[1] }, sigma1, sigma2, DogDetection.ExtremaType.MINIMA,
					threshold, true);

			if (newdog.getSubpixelPeaks().size() < 100){
			// Detect minima in Scale space
			SubpixelMinlist = newdog.getSubpixelPeaks();
			}

						
			for (int index = 0; index < SubpixelMinlist.size(); ++index) {

				
				
				final Staticproperties statprops = new Staticproperties(objproperties.Label, framenumber,
						objproperties.diameter,
						new double[] { SubpixelMinlist.get(index).getDoublePosition(0),
								SubpixelMinlist.get(index).getDoublePosition(1) },
						objproperties.totalintensity, objproperties.Circularity);
				System.out.println( "Location: " + "" + SubpixelMinlist.get(index).getDoublePosition(0) +
						" " + SubpixelMinlist.get(index).getDoublePosition(1)
						+ "Circularity" + " " + objproperties.Circularity);
				// System.out.println(label + " " + estimatedDiameter );
				staticprops.add(statprops);

			}
		}

		return staticprops;
	}

}