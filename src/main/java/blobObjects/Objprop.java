package blobObjects;

	
	public final class Objprop {

        public  int Label;
		public  double diameter;
		public double[] sigma;
		public  double totalintensity;
		public double[] location;
	

		public Objprop(final int Label, final double diameter,final double totalintensity) {
			this.Label = Label;
			this.diameter = diameter;
			this.totalintensity = totalintensity;
			

		}
		
		public Objprop(final int Label, final double diameter, final double[] location, final double[] sigma, final double totalintensity){
			
			this.Label = Label;
			this.sigma = sigma;
			this.totalintensity = totalintensity;
			this.location = location;
			this.diameter = diameter;
			
			
			
		}
		
		
		
	}



