package segmentBlobs;

import java.util.ArrayList;

import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Segmentbywatershed {

	
	
	
	public static RandomAccessibleInterval<IntType> getsegmentedimage(final RandomAccessibleInterval<FloatType> blobimage){
		
		RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage, new IntType());
		
		labelledimage = segmentBlobs.Watersheddding.Dowatersheddingonly(blobimage);
		
		
		
		
		return labelledimage;
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList<Staticproperties> DoGdetection(final IntervalView<FloatType> blobimage,
			RandomAccessibleInterval<IntType> labelledimage){
		final int ndims = blobimage.numDimensions();
		
		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(ndims);
		
		ArrayList<Staticproperties> staticprops = new ArrayList<Staticproperties>(ndims);
		
		// Get the seed image used for watershedding, this is used to get object properties
		
		NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(blobimage, new IntType()));
		
		oldseedLabeling = Watersheddding.PrepareSeedImage(blobimage);
		
		RandomAccessibleInterval<IntType>  objectlabels = oldseedLabeling.getStorageImg();
		
		final Getobjectproperties props = new  Getobjectproperties(blobimage, objectlabels);
		
		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);
		

		// Background is lablled as 0 so start from 1 to Maxlabel - 1 					
		
		for (int label = 1; label < Maxlabel - 1; ++label ){
			RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(blobimage,
					new FloatType());
			 final Objprop objproperties = props.Getobjectprops(label);
			
			final double estimatedradius = objproperties.diameter/2;
			
			outimg = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
			
			// Determine local threshold value for each label
			final Float val = GlobalThresholding.AutomaticThresholding(outimg);
			
			final FinalInterval range = new FinalInterval(outimg.dimension(0), outimg.dimension(1));
			
			if (estimatedradius > 0){
			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendMirrorSingle(outimg), range,
					new double[] { 1, 1 }, estimatedradius, estimatedradius + 0.1,
					DogDetection.ExtremaType.MINIMA,
					 val, true);
			
			// Detect minima in Scale space
			SubpixelMinlist = newdog.getSubpixelPeaks();
			if (SubpixelMinlist.isEmpty() == false){
              int index = SubpixelMinlist.size() - 1;
			
					final Staticproperties statprops = new Staticproperties(label, objproperties.diameter, objproperties.Area,
					new double[] {SubpixelMinlist.get(index).getDoublePosition(0),
							SubpixelMinlist.get(index).getDoublePosition(1)}, objproperties.totalintensity);
					
						
					
				staticprops.add(statprops);
            
			}
		}
		}
		
		return staticprops;
	}
	
	
	
}
