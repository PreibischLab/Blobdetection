package segmentBlobs;

import java.util.ArrayList;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Getmeanintensity {

	
	public static double meanblobintensity(RandomAccessibleInterval<FloatType> currentframe, 
			RandomAccessibleInterval<IntType> currentlabelledimage, int label){
		
		final RandomAccess<IntType> intranac = currentlabelledimage.randomAccess();
		
			double avgintensity = 0;
			if (intranac.get().get() == label){
			final RealSum realSumA = new RealSum();
			long countA = 0;

			for (final FloatType type : Views.iterable(currentframe)) {
				realSumA.add(type.getRealDouble());
				++countA;
			}
			
			 avgintensity = realSumA.getSum() / countA;
			
			}
			
			return avgintensity;
		}
		
		
		
}
