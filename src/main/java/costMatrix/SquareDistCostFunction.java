package costMatrix;

import segmentBlobs.Staticproperties;

/**
 * A cost function that returns cost equal to the square distance. Suited to
 * Brownian motion.
 * 
 * @author Jean-Yves Tinevez - 2014
 * 
 */
public class SquareDistCostFunction implements CostFunction< Staticproperties, Staticproperties >
{

	@Override
	public double linkingCost( final Staticproperties source, final Staticproperties target )
	{
		return source.squareDistanceTo(target );
	}

}
