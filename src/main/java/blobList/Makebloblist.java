package blobList;

import java.util.ArrayList;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import segmentBlobs.Staticproperties;

public class Makebloblist {

	
	public static ArrayList<Staticproperties> returnBloblist(final IntervalView<FloatType> baseframe, 
			RandomAccessibleInterval<FloatType> preinputimg, final int minDiameter, final int maxDiameter, final double[] calibration, int framenumber,  final boolean softThreshold){
		
		RandomAccessibleInterval<IntType> labelledimagebase = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());

	
		// Segmenting the image via watershed
		labelledimagebase = segmentBlobs.Segmentbywatershed.getsegmentedimage(preinputimg, softThreshold);
		
		// List containing all the maximas in baseframe
		ArrayList<Staticproperties> Spotmaxbase = segmentBlobs.Segmentbywatershed.DoGdetection(baseframe,
				labelledimagebase, minDiameter, maxDiameter, calibration, framenumber, softThreshold);
		
		
		return Spotmaxbase;
	}
	
	
	
	public static void remove( final Staticproperties spot, final ArrayList<ArrayList<Staticproperties>> blobsinframe, final Integer frame )
	{
		
		 blobsinframe.get(frame).remove( spot );
	}
	
	public static void add( final Staticproperties spot, final ArrayList<ArrayList<Staticproperties>> blobsinframe, final Integer frame )
	{
		
		 blobsinframe.get(frame).add( spot );
	}
	
	
}
