package nearestNeighbour;

import java.util.ArrayList;

import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealCursor;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

public class NearestNeighboursearch {

	public static void NearestNeighbour(IntervalView<FloatType> baseframe, IntervalView<FloatType> targetframe,
			final double[] estimatedradius){
		
		final int ndims = baseframe.numDimensions();
		
		assert baseframe.numDimensions() == targetframe.numDimensions();
		
		
		RandomAccessibleInterval<IntType> labelledimagebase = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());

		RandomAccessibleInterval<IntType> labelledimagetarget = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());
		// Find Maxima of blobs by segmenting the image via watershed
		labelledimagebase = segmentBlobs.Segmentbywatershed.getsegmentedimage(baseframe);
		labelledimagetarget = segmentBlobs.Segmentbywatershed.getsegmentedimage(targetframe);
		
		System.out.println("Segmenting base image and doing DoG detection:");
		// List containing all the maximas in baseframe
		ArrayList<RefinedPeak<Point>> Spotmaxbase = segmentBlobs.Segmentbywatershed.DoGdetection(baseframe,
				labelledimagebase, estimatedradius);
		System.out.println("Done!");
		System.out.println("Segmenting target image and doing DoG detection:");
		// List containing all the maximas in targetframe
		ArrayList<RefinedPeak<Point>> Spotmaxtarget = segmentBlobs.Segmentbywatershed.DoGdetection(targetframe,
				labelledimagetarget, estimatedradius);
		System.out.println("Done!");
		
		final RealPointSampleList<Double> baselist = new RealPointSampleList<Double>(baseframe.numDimensions());
		
		for (int index = 0; index < Spotmaxbase.size(); ++index){
			RealPoint basepoint = new RealPoint(Spotmaxbase.get(index));
			
			baselist.add(basepoint, Spotmaxbase.get(index).getValue());
		}
		
      final RealPointSampleList<Double> targetlist = new RealPointSampleList<Double>(targetframe.numDimensions());
		
		for (int index = 0; index < Spotmaxtarget.size(); ++index){
			RealPoint targetpoint = new RealPoint(Spotmaxtarget.get(index));
			
			targetlist.add(targetpoint, Spotmaxtarget.get(index).getValue());
		}
		
		
		System.out.println(" Making KD Tree: ");
		
		final KDTree<Double> tree = new KDTree<Double>(targetlist);
		
		final NearestNeighborSearchOnKDTree<Double> search = new NearestNeighborSearchOnKDTree<Double>(tree);
		
		RealCursor<Double> listcursor = baselist.localizingCursor();
		
		while(listcursor.hasNext()){
			
			listcursor.fwd();
			
			search.search(listcursor);
			
			System.out.println("Base frame point X: " + listcursor.getDoublePosition(0)+ " Y: " +  listcursor.getDoublePosition(1));
			System.out.println("Nearest neighbour in target frame X: " + search.getPosition().getDoublePosition(0) +  " Y: " 
			+ search.getPosition().getDoublePosition(1) );
		}
		
	}
	
	
	
}
