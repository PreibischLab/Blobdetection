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

	
	
	
	public static RandomAccessibleInterval<IntType> getsegmentedimage(final RandomAccessibleInterval<FloatType> blobimage){
		
		RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage, new IntType());
		
		labelledimage = segmentBlobs.Watersheddding.Dowatersheddingonly(blobimage);
		
	//	ImageJFunctions.show(labelledimage);
		return labelledimage;
	}
	
	public static ArrayList<Staticproperties> DoGdetection(final IntervalView<FloatType> blobimage,
			final RandomAccessibleInterval<IntType> labelledimage, final int minRadius, final int maxRadius){
		final int ndims = blobimage.numDimensions();
		
		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(ndims);
		
		ArrayList<Staticproperties> staticprops = new ArrayList<Staticproperties>(ndims);
		
		
		final Getobjectproperties props = new  Getobjectproperties(blobimage, labelledimage, minRadius, maxRadius);
		
		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);
		

		// Background is lablled as 0 so start from 1 to Maxlabel - 1 					
		
		for (int label = 1; label < Maxlabel - 1; ++label ){
			RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(blobimage,
					new FloatType());
			 final Objprop objproperties = props.Getobjectprops(label);
			
			 
			final double estimatedDiameter = objproperties.diameter ;
			
			outimg = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
			
			// Determine local threshold value for each label
			final Float val = GlobalThresholding.AutomaticThresholding(outimg);
			
			final FinalInterval range = new FinalInterval(outimg.dimension(0), outimg.dimension(1));
			double sigma1 = 1.0/(1+ Math.sqrt(2)) * estimatedDiameter;
			double sigma2 = Math.sqrt(1.2) * sigma1;
			
			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendMirrorSingle(outimg), range,
					new double[] { 1, 1 }, sigma1, sigma2,
					DogDetection.ExtremaType.MINIMA,
					 val, true);
			
			// Detect minima in Scale space
			SubpixelMinlist = newdog.getSubpixelPeaks();
			
			
            for (int index = 0; index < SubpixelMinlist.size(); ++index ){
			
					final Staticproperties statprops = new Staticproperties(objproperties.Label, objproperties.diameter, 
					new double[] {SubpixelMinlist.get(index).getDoublePosition(0),
							SubpixelMinlist.get(index).getDoublePosition(1)}, objproperties.totalintensity, GetLocalmaxmin.computeMaxIntensity(blobimage), GetLocalmaxmin.computeMinIntensity(blobimage));
					
						
					//System.out.println(label + " " + estimatedDiameter  );
				staticprops.add(statprops);
            
			
		}
		}
		
		return staticprops;
	}
	
	
	
}
