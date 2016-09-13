package segmentBlobs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class Staticproperties {

	

		public final int Label;
		public final double maxextent;
		public final int Area;
		public final double[] location;
		public final double Intensity;
		
		protected Staticproperties(final int Label, final double maxextent, final int Area, final double[] location,
				final double Intensity) {
			this.Label = Label;
			this.maxextent = maxextent;
			this.Area = Area;
			this.location = location;
			this.Intensity = Intensity;

		}

	
		
		
		
		
		
		
}
