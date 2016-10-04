package costMatrix;

import segmentBlobs.Staticproperties;

public class IntensityDiffCostFunction implements CostFunction< Staticproperties, Staticproperties >
	{

		
	

	@Override
	public double linkingCost( final Staticproperties source, final Staticproperties target )
	{
		return source.IntensityweightedsquareDistanceTo(target );
	}
		

	
}
