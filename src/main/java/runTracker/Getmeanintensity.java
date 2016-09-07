package runTracker;

import java.util.ArrayList;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.IntervalView;
import segmentBlobs.Labeledblob;
import segmentBlobs.Watersheddding;

public class Getmeanintensity {

	
	public static void meanblobintensity(IntervalView<FloatType> currentframe, RandomAccessibleInterval<IntType> currentlabelledimage){
		
		final int Maxlabel = Watersheddding.GetMaxlabelsseeded(currentlabelledimage);
		final RandomAccess<IntType> intranac = currentlabelledimage.randomAccess();
		ArrayList<Labeledblob> allblobs = new ArrayList<Labeledblob>();
		
		for (int label = 1; label < Maxlabel -1; ++label){
			double avgintensity = 0;
			if (intranac.get().get() == label){
			final RealSum realSumA = new RealSum();
			long countA = 0;

			for (final FloatType type : currentframe) {
				realSumA.add(type.getRealDouble());
				++countA;
			}
			
			 avgintensity = realSumA.getSum() / countA;
			
			}
			
			Labeledblob newblob = new Labeledblob(label, avgintensity);
			allblobs.add(newblob);
		}
		
		
		
	}
}
