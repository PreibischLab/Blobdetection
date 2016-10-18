package blobList;

import java.util.ArrayList;
import java.util.Set;

import blobObjects.Objprop;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import segmentBlobs.Getobjectproperties;
import segmentBlobs.Staticproperties;

public class Makebloblist {

	public static ArrayList<Staticproperties> returnBloblist(final IntervalView<FloatType> baseframe,
			RandomAccessibleInterval<FloatType> preinputimg, final int estimatedDiamter,
			final double[] calibration, int framenumber, final boolean softThreshold) {

		RandomAccessibleInterval<IntType> labelledimagebase = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());
		final int ndims = baseframe.numDimensions();

		// Segmenting the image via watershed
		labelledimagebase = segmentBlobs.Segmentbywatershed.getsegmentedimage(preinputimg, softThreshold);

		// List containing all the maximas in baseframe
		ArrayList<RefinedPeak<Point>> SubpixelMinlist = segmentBlobs.Segmentbywatershed.DoGdetection(baseframe,
				labelledimagebase, estimatedDiamter, calibration, softThreshold);
		ArrayList<Staticproperties> staticprops = new ArrayList<Staticproperties>(ndims);
		
		for (int index = 0; index < SubpixelMinlist.size() ; ++index ){
		
		final Getobjectproperties props = new Getobjectproperties(baseframe, labelledimagebase,SubpixelMinlist.get(index), estimatedDiamter );
		
		final Objprop objproperties = props.Getobjectprops();
		
		final Staticproperties statprops = new Staticproperties( framenumber,
				objproperties.diameter,
				new double[] { SubpixelMinlist.get(index).getDoublePosition(0),
						SubpixelMinlist.get(index).getDoublePosition(1) },
				objproperties.totalintensity);

		 if (objproperties.diameter > 0)
		
		staticprops.add(statprops);
		
		}
		
		return staticprops;
	}

	public static void remove(final Staticproperties spot, final ArrayList<ArrayList<Staticproperties>> blobsinframe,
			final Integer frame) {

		blobsinframe.get(frame).remove(spot);
	}

	public static void add(final Staticproperties spot, final ArrayList<ArrayList<Staticproperties>> blobsinframe,
			final Integer frame) {

		blobsinframe.get(frame).add(spot);
	}

}
