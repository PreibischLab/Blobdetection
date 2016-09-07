package segmentBlobs;

import java.util.ArrayList;

import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class Segmentbywatershed {

	public static RandomAccessibleInterval<IntType> getsegmentedimage(final RandomAccessibleInterval<FloatType> blobimage){
		
		RandomAccessibleInterval<IntType> labelledimage = new ArrayImgFactory<IntType>().create(blobimage, new IntType());
		
		labelledimage = segmentBlobs.Watersheddding.Dowatersheddingonly(blobimage);
		
		
		
		
		return labelledimage;
	}
	
	public static ArrayList<RefinedPeak<Point>> DoGdetection(final RandomAccessibleInterval<FloatType> blobimage,
			RandomAccessibleInterval<IntType> labelledimage, final double[] sizeofblob){
		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(blobimage.numDimensions());
		ArrayList<RefinedPeak<Point>> TotalMinlist = new ArrayList<RefinedPeak<Point>>(blobimage.numDimensions());
		
		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(labelledimage);
		// Background is lablled as 0 so start from 1 to Maxlabel - 1 
		
		
		for (int label = 1; label < Maxlabel - 1; ++label ){
			RandomAccessibleInterval<FloatType> outimg = new ArrayImgFactory<FloatType>().create(blobimage,
					new FloatType());
			
			outimg = Watersheddding.CurrentLabelImage(labelledimage, blobimage, label);
			final Float val = GlobalThresholding.AutomaticThresholding(outimg);
			final FinalInterval range = new FinalInterval(outimg.dimension(0), outimg.dimension(1));
			
			DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendMirrorSingle(outimg), range,
					new double[] { 1, 1 }, Math.min(sizeofblob[0], sizeofblob[1]), Math.min(sizeofblob[0], sizeofblob[1]) + 0.1,
					DogDetection.ExtremaType.MINIMA,
					 val, true);
			
			// Detect minima in Scale space
			SubpixelMinlist = newdog.getSubpixelPeaks();
			
              for (int index = 0; index < SubpixelMinlist.size(); ++index){
				
				  TotalMinlist.add(SubpixelMinlist.get(index));
			}
            
             
		}
	
		
		return TotalMinlist;
	}
	
	
	
}
