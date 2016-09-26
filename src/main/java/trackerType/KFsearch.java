package trackerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;

import net.imglib2.RealPoint;
import java.util.HashSet;
import net.imglib2.algorithm.Benchmark;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import costMatrix.CostFunction;
import costMatrix.SquareDistCostFunction;
import graphconstructs.Logger;
import segmentBlobs.Staticproperties;

public class KFsearch implements Blob {

	
	private final ArrayList<ArrayList<Staticproperties>> Allblobs;
	private final double maxsearchRadius;
	private final double initialsearchRadius;
	private final int maxframe;
	private final int maxframeGap;
	private SimpleWeightedGraph< Staticproperties, DefaultWeightedEdge > graph;
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;

	public KFsearch(
			final ArrayList<ArrayList<Staticproperties>> Allblobs, final double maxsearchRadius, final double initialsearchRadius, 
			final int maxframe, final int maxframeGap){
		this.Allblobs = Allblobs;
		this.initialsearchRadius = initialsearchRadius;
		this.maxsearchRadius = maxsearchRadius;
		this.maxframe = maxframe;
		this.maxframeGap = maxframeGap;
		
	}
	
	@Override
	public boolean process() {
		
		
		
		// Find first two non-zero frames containing blobs
		
		
		
		Iterator<ArrayList<Staticproperties>> Allblobsiterator = Allblobs.iterator();
		int Firstframe = 0;
		int Secondframe = 0;
		
		for (int frame = 0; frame < maxframe - 1 ; ++frame){
			
			if (Allblobs.get(frame).size() > 0){
				Firstframe = frame;
				break;
			}
		}
		
         for (int frame = Firstframe + 1; frame < maxframe - 1 ; ++frame){
			
			if (Allblobs.get(frame).size() > 0){
				Secondframe = frame;
				break;
			}
		}
		
		ArrayList<Staticproperties> Firstorphan = Allblobs.get(Firstframe);
		
		ArrayList<Staticproperties> Secondorphan = Allblobs.get(Secondframe);
		
		
		
		
		for (int frame = Secondframe; frame < maxframe - 1; ++frame){
			
			
			ArrayList<Staticproperties> Spotmaxbase = Allblobs.get(frame);
			
			ArrayList<Staticproperties> Spotmaxtarget = Allblobs.get(frame + 1);
		
			
			
			
		
		
		// Max KF search cost.
		final double maxCost = maxsearchRadius * maxsearchRadius;
		// Cost function to nucleate KFs.
		final CostFunction< Staticproperties, Staticproperties > nucleatingCostFunction = new SquareDistCostFunction();
		// Max cost to nucleate KFs.
		final double maxInitialCost = initialsearchRadius * initialsearchRadius;

		/*
		 * Estimate Kalman filter variances.
		 *
		 * The search radius is used to derive an estimate of the noise that
		 * affects position and velocity. The two are linked: if we need a large
		 * search radius, then the fluoctuations over predicted states are
		 * large.
		 */
		final double positionProcessStd = maxsearchRadius / 3d;
		final double velocityProcessStd = maxsearchRadius / 3d;
		
		final Map< CVMKalmanFilter, Staticproperties > kalmanFiltersMap = 
				new HashMap< CVMKalmanFilter, Staticproperties >( Spotmaxtarget.size() );
		
		// Make the preditiction map
		final Map< RealPoint, CVMKalmanFilter > predictionMap = 
				new HashMap< RealPoint, CVMKalmanFilter >( kalmanFiltersMap.size() );
		for ( final CVMKalmanFilter kf : kalmanFiltersMap.keySet() )
		{
			final double[] X = kf.predict();
			final RealPoint point = new RealPoint( X );
			predictionMap.put( point, kf );

			}
		
		
		
		
		}
		
		return true;
		}
		
		
	
	@Override
	public void setLogger( final Logger logger) {
		this.logger = logger;
		
	}
	

	@Override
	public SimpleWeightedGraph< Staticproperties, DefaultWeightedEdge > getResult()
	{
		return graph;
	}
	
	@Override
	public boolean checkInput() {
		final StringBuilder errrorHolder = new StringBuilder();;
		final boolean ok = checkInput();
		if (!ok) {
			errorMessage = errrorHolder.toString();
		}
		return ok;
	}
	
	

	@Override
	public String getErrorMessage() {
		
		return errorMessage;
	}
}
