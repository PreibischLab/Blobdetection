package gaussianFits;

public class TwoDimensionalGauss implements FitFunction {


	/**
	 Gaussian peak with constant noise
	 */

		/*
		 * Gaussian parameters with the const noise term to be determined by the solver
		 */

		@Override
		public final double val(final double[] x, final double[] a) {
			return a[0] * E(x, a) + a[2*x.length + 2];
		}

		/**
		 * Partial derivatives indices are ordered as follow:
		 * <pre>k = 0       - A
		 *k = 1..n    - x_i (with i = k-1)
		 *k = n+1..2n - b_i (with i = k-n-1)</pre> 
		 */
		@Override
		public final double grad(final double[] x, final double[] a, final int k) {
			final int ndims = x.length;
			
			double dx = x[0] - a[1];
			double dy = x[1] - a[2];
			double corrxx = a[ndims + 1];
			double corryy = a[ndims + 2];
			double corrxy = a[2 * ndims + 1];
			if (k == 0) {
				// With respect to A
				return E(x, a) ;

			} 
			else if (k == 1){
				
				return     (2 * corrxx * dx - corrxy * dy * Math.sqrt(corrxx * corryy)) * a[0]  * E(x, a) ;
			}
			
            else if (k == 2){
				
				return     (2 * corryy * dy - corrxy * dx * Math.sqrt(corrxx * corryy)) * a[0]  * E(x, a) ;
			}
			
            else if (k == 3){
            	
            	return   -(dx * dx - (0.5/Math.sqrt(corrxx)) *   dx * dy * corrxy * Math.sqrt(corryy) ) 
            			* a[0] * E(x, a) ;
            }
            else if (k == 4){
            	
            	return 
            			-(dy * dy - (0.5/Math.sqrt(corryy)) *  dx * dy * corrxy * Math.sqrt(corrxx) ) 
            			* a[0] * E(x, a) ;
            }
			
            else if (k == 5){
            	
            	return (  dx * dy * Math.sqrt(corrxx * corryy))* a[0] * E(x, a);
            
            }
			else{
				
				return 1.0;
			}
		}

		

		/*
		 * PRIVATE METHODS
		 */

		private static final double E(final double[] x, final double[] a) {
			final int ndims = x.length;
			double sum = 0;
			double di;
			
			double dx = x[0] - a[1];
			double dy = x[1] - a[2];
			double corrxx = a[ndims + 1];
			double corryy = a[ndims + 2];
			double corrxy = a[2 * ndims + 1];
			
			sum = corrxx * dx * dx + corryy * dy * dy -  corrxy*dx *dy *Math.sqrt(corrxx * corryy);
			
			
			return Math.exp(-sum);
		}
		

	}


