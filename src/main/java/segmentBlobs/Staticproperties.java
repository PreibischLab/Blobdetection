package segmentBlobs;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.RealLocalizable;

public final class Staticproperties  implements RealLocalizable, Comparable< Staticproperties > {

	
	/**
	 * @param maxextent
	 *                the estimated diamter of the blob
	 * @param currentframe
	 *                the current frame
	 * @param location
	 *                the real location of the blob. 
	 * @param sigma 
	 *                the sigma of the blobs if using 2D Gaussian fits, else 
	 *                maxextent value in both dimensions.
	 * @param Intensity
	 *                the total intensity of the blob.
	 * @param corr
	 *               the correlation term when doing 2D Gaussian fits
	 * @param Circularity
	 *               the Circularity value for the blob                                                                        
	 * 
	 */

		public  double maxextent;
		public int currentframe;
		public  double[] location;
		public double[] sigma;
		public  double Intensity;
		public double corr;
		public double Circularity;
		
		// Parameter for the cost function to decide how much weight to give to Intensity and to distance
		/*
		 * CONSTRUCTORS
		 */

		/**
		 * Creates a new Blob.
		 *
		 * @param Label
		 *            the watershed Label to which the Blob belongs to.
		 * @param maxextent
		 *            Estimated diameter of the Blob, estimated via putting rings of increasing radii, in image units.
		 * @param location[]
		 *            DoG detected location of the Blob, in image units.
		 * @param Intensity
		 *            Intensity of the Blob in image units
		 */
		
		public Staticproperties(final int Label, final int currentframe, final double maxextent, final double[] location,
				final double Intensity, final double Circularity) {
			this.currentframe = currentframe;
			this.maxextent = maxextent;
			this.location = location;
			this.Intensity = Intensity;
			this.Circularity = Circularity;
			

		}
		
		/**
		 * Creates a new Blob.
		 *
		 * @param Label
		 *            the watershed Label to which the Blob belongs to.
		 * @param maxextent
		 *            Estimated diameter of the Blob, estimated via putting rings of increasing radii, in image units.
		 * @param location[]
		 *            DoG detected location of the Blob, in image units.
		 * @param Intensity
		 *            Intensity of the Blob in image units
		 */
		
		public Staticproperties(final int Label, final int currentframe,  final double[] location,final double[] sigma, final double corr,
				final double noise,
				final double maxextent,
				final double Intensity,
				final double Circularity) {
			this.currentframe = currentframe;
			this.sigma = sigma;
			this.location = location;
			this.Intensity = Intensity;
			this.maxextent = maxextent;
			this.corr = corr;
			this.Circularity = Circularity;

		}
		
		/**
		 * Creates a new Blob.
		 *
		 * @param maxextent
		 *            Estimated diameter of the Blob, estimated via putting rings of increasing radii, in image units.
		 * @param location[]
		 *            DoG detected location of the Blob, in image units.
		 * @param Intensity
		 *            Intensity of the Blob in image units
		 */
		
		public Staticproperties(final int currentframe, final double maxextent, final double[] location,
				final double Intensity, final double Circularity) {
			this.currentframe = currentframe;
			this.maxextent = maxextent;
			this.location = location;
			this.Intensity = Intensity;
			this.Circularity = Circularity;
			

		}
		
		
		public Staticproperties(final int currentframe,  final double[] location,final double[] sigma, double corr,
				final double Intensity, final double Circularity) {
			this.currentframe = currentframe;
			this.sigma = sigma;
			this.location = location;
			this.Intensity = Intensity;
			this.corr = corr;
			this.Circularity = Circularity;

		}
		/**
		 * Creates a new Blob.
		 *
		 * @param maxextent
		 *            Estimated diameter of the Blob, estimated via putting rings of increasing radii, in image units.
		 * @param RealLocalizable
		 *            Location of the Blob, in image units.
		 * @param Intensity
		 *            Intensity of the Blob in image units
		 */
		
		public Staticproperties(final int currentframe, final double maxextent, final RealLocalizable location,
				final double Intensity, final double Circularity) {
			
			this(currentframe, maxextent, 
					new double [] {location.getDoublePosition(0) , location.getDoublePosition(1)}, Intensity, Circularity);
			
			

		}
		
		
		
		
		
		
		/**
		 * Returns the squared distance between two blobs.
		 *
		 * @param target
		 *            the Blob to compare to.
		 *
		 * @return the distance to the current blob to target blob specified.
		 */
		
		public double squareDistanceTo(Staticproperties target) {
			// Returns squared distance between the source Blob and the target Blob.
			
			final double[] sourceLocation = location;
			final double[] targetLocation = target.location;
			
			double distance = 0;
			
			for (int d = 0; d < sourceLocation.length; ++d){
				
				distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
			}
			
			
			return distance;
		}
		
		
		/**
		 * Returns the Intnesity weighted squared distance between two blobs.
		 *
		 * @param target
		 *            the Blob to compare to.
		 *
		 * @return the Intensity weighted distance to the current blob to target blob specified.
		 */
		
		public double IntensityweightedsquareDistanceTo(Staticproperties target) {
			// Returns squared distance between the source Blob and the target Blob.
			
			final double[] sourceLocation = location;
			final double[] targetLocation = target.location;
			
			double distance = 0;
			
			for (int d = 0; d < sourceLocation.length; ++d){
				
				distance += (sourceLocation[d]  - targetLocation[d])  * (sourceLocation[d]  - targetLocation[d] );
			}
			
			double IntensityweightedDistance =  (distance )* Math.pow((Intensity / target.Intensity), 2) ;
			
			return IntensityweightedDistance;
		}
		
		

	
		/**
		 * Returns the difference between the location of two blobs, this operation
		 * returns (
		 * <code>A.diffTo(B) = - B.diffTo(A)</code>)
		 *
		 * @param target
		 *            the Blob to compare to.
		 * @param int n
		 *            n = 0 for X- coordinate, n = 1 for Y- coordinate
		 * @return the difference in co-ordinate specified.
		 */
		public double diffTo( final Staticproperties target, int n )
		{
			
			final double thisBloblocation = location[n];
			final double targetBloblocation = target.location[n];
			return thisBloblocation - targetBloblocation;
		}
		
		/**
		 * Returns the difference between the Intensity of two blobs, this operation
		 * returns (
		 * <code>A.diffTo(B) = - B.diffTo(A)</code>)
		 *
		 * @param target
		 *            the Blob to compare to.
		 * 
		 * @return the difference in Intensity of Blobs.
		 */
		public double diffTo( final Staticproperties target)
		{
			final double thisBloblocation = Intensity;
			final double targetBloblocation = target.Intensity;
			return thisBloblocation - targetBloblocation;
		}
		
		
		


		@Override
		public int compareTo(Staticproperties o) {
			
			return hashCode() - o.hashCode();
		}


		@Override
		public void localize(float[] position) {
			int n = position.length;
			for ( int d = 0; d < n; ++d )
				position[ d ] = getFloatPosition( d );
			
		}


		@Override
		public void localize(double[] position) {
			int n = position.length;
			for ( int d = 0; d < n; ++d )
				position[ d ] = getDoublePosition( d );			
		}


		@Override
		public float getFloatPosition(int d) {
			return ( float ) getDoublePosition( d );
		}


		@Override
		public double getDoublePosition(int d) {
			return getDoublePosition( d );
		}


		@Override
		public int numDimensions() {
			
			return location.length;
		}
		
		
}
