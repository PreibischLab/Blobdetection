package blobList;

import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import segmentBlobs.Staticproperties;

public class Makebloblist {

	
	public static ArrayList<Staticproperties> returnBloblist(final IntervalView<FloatType> baseframe, 
			final int minDiameter, final int maxDiameter){
		
		RandomAccessibleInterval<IntType> labelledimagebase = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());

	
		// Find Maxima of blobs by segmenting the image via watershed
		labelledimagebase = segmentBlobs.Segmentbywatershed.getsegmentedimage(baseframe);
		
		// List containing all the maximas in baseframe
		ArrayList<Staticproperties> Spotmaxbase = segmentBlobs.Segmentbywatershed.DoGdetection(baseframe,
				labelledimagebase, minDiameter, maxDiameter);
		
		
		return Spotmaxbase;
	}
}