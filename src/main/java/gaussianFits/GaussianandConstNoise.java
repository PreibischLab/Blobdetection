package gaussianFits;


	/**
	 Gaussian peak with constant noise
	 */
	public class GaussianandConstNoise implements FitFunction {

		/*
		 * Gaussian parameters with the const noise term to be determined by the solver
		 */

		@Override
		public final double val(final double[] x, final double[] a) {
			return a[0] * E(x, a) + a[2*x.length + 1];
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
			if (k == 0) {
				// With respect to A
				return E(x, a);

			} else if (k <= ndims) {
				// With respect to xi
				int dim = k - 1;
				return 2 * a[dim+ndims] * (x[dim] - a[dim+1]) * a[0] * E(x, a);

			} else if (k > ndims && k < 2*ndims + 1) {
				// With respect to ai
				int dim = k - ndims - 1;
				double di = x[dim] - a[dim+1];
				return - di * di * a[0] * E(x, a);
			}
			else{
				
				return 1.0;
			}
		}

		/**
		 * Not used but hey.
		 * @return the hessian value for row r and column c
		 */
		public final double hessian(final double[] x, final double[] a, int r, int c) {
			if (c < r) {
				int tmp = c;
				c = r;
				r = tmp;
			} // Ensure c >= r, top right half the matrix

			final int ndims = x.length;

			if (r == 0) {
				// 1st line

				if (c ==0) {
					return 0;

				} else if (c <= ndims ) {
					// d²G / (dA dxi)
					final int dim = c - 1;
					return 2 * a[dim+ndims] * (x[dim] - a[dim+1])  * E(x, a);

				} else  {
					// d²G / (dA dsi)
					final int dim = c - ndims - 1;
					final double di = x[dim] - a[dim+1];
					return - di * di * E(x, a);
				}
				

			} else if (c == r) {
				// diagonal

				if (c <= ndims ) {
					// d²G / dxi²
					final int dim = c - 1;
					final double di = x[dim] - a[dim+1];
					return 2 * a[0] * E(x, a) * a[dim+ndims] * ( 2 * a[dim+ndims] * di * di - 1 );

				} else {
					// d²G / dsi²
					final int dim = c - ndims - 1;
					final double di = x[dim] - a[dim+1];
					return a[0] * E(x, a) * di * di * di * di;
				}

			} else if ( c <= ndims && r <= ndims ) {
				// H1
				// d²G / (dxj dxi)
				final int i = c - 1;
				final int j = r - 1;
				final double di = x[i] - a[i+1];
				final double dj = x[j] - a[j+1];
				return 4 * a[0] * E(x, a) * a[i+ndims] * a[j+ndims] * di * dj;

			} else if ( r <= ndims && c > ndims) {
				// H3
				// d²G / (dxi dsj)
				final int i = r - 1; // xi
				final int j = c - ndims - 1; // sj
				final double di = x[i] - a[i+1];
				final double dj = x[j] - a[j+1];
				return - 2 * a[0] * E(x, a) * a[i+ndims] * di * ( 1 - a[j+ndims] * dj * dj);

			} else {
				// H2
				// d²G / (dsj dsi)
				final int i = r - ndims - 1; // si
				final int j = c - ndims - 1; // sj
				final double di = x[i] - a[i+1];
				final double dj = x[j] - a[j+1];
				return a[0] * E(x, a) * di * di * dj * dj;
			}

		}

		/*
		 * PRIVATE METHODS
		 */

		private static final double E(final double[] x, final double[] a) {
			final int ndims = x.length;
			double sum = 0;
			double di;
			for (int i = 0; i < x.length; i++) {
				di = x[i] - a[i+1];
				sum += a[i+ndims+1] * di * di;
			}
			return Math.exp(-sum);
		}
		

	}
