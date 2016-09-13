package segmentBlobs;

	
	public final class Objprop {

		public final int Label;
		public final double diameter;
		public final int Area;
		public final double totalintensity;
		public final double[] startpoint;
		public final double[] endpoint;

		protected Objprop(final int Label, final double diameter, final int Area, final double totalintensity, final double[] startpoint,
				final double[] endpoint) {
			this.Label = Label;
			this.diameter = diameter;
			this.Area = Area;
			this.totalintensity = totalintensity;
			this.startpoint = startpoint;
			this.endpoint = endpoint;

		}
	}



