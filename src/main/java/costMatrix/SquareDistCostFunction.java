package costMatrix;

import segmentBlobs.Staticproperties;

/**
 * Implementation of various cost functions
 * 
 * 
 */

// Cost function base don minimizing the squared distances

public class SquareDistCostFunction implements CostFunction< Staticproperties, Staticproperties >
{

	@Override
	public double linkingCost( final Staticproperties source, final Staticproperties target )
	{
		return source.squareDistanceTo(target );
	}
	
	
	
	

}
