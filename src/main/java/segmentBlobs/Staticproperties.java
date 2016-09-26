package segmentBlobs;



public final class Staticproperties {

	

		public final int Label;
		public final double maxextent;
		public final double Area;
		public final double[] location;
		public final double Intensity;
		
		
		protected Staticproperties(final int Label, final double maxextent, final double Area, final double[] location,
				final double Intensity) {
			this.Label = Label;
			this.maxextent = maxextent;
			this.Area = Area;
			this.location = location;
			this.Intensity = Intensity;

		}
		
		
		public double squareDistanceTo(Staticproperties target) {
			// Returns squared distance between the source Blob and the target Blob.
			
			final double[] sourceLocation = this.location;
			final double[] targetLocation = target.location;
			
			double distance = 0;
			
			for (int d = 0; d < sourceLocation.length; ++d){
				
				distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
			}
			
			
			return distance;
		}

	
		
		
		
		
}
