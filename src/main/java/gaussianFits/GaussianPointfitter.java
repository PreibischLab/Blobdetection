package gaussianFits;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import segmentBlobs.Watersheddding;

public class GaussianPointfitter {

		private final RandomAccessibleInterval<FloatType> inputimg;
		private final RandomAccessibleInterval<IntType> intimg;
		private final int currentlabel;
		private final int ndims;

		public GaussianPointfitter(RandomAccessibleInterval<FloatType> inputimg, RandomAccessibleInterval<IntType> intimg, final int currentlabel) {

			this.inputimg = inputimg;
			this.intimg = intimg;
			this.currentlabel = currentlabel;
			this.ndims = inputimg.numDimensions();
			assert inputimg.numDimensions() == intimg.numDimensions();

		}
		
		private final double[] makeBestGuess(final Localizable point, final double[][] X, final double[] I) {

			double[] start_param = new double[2 * ndims + 3];

			double I_sum = 0;
			double[] X_sum = new double[ndims];
			for (int j = 0; j < ndims; j++) {
				X_sum[j] = 0;
				for (int i = 0; i < X.length; i++) {
					X_sum[j] += X[i][j] * I[i];
				}
			}
			double max_I = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < X.length; i++) {
				I_sum += I[i];
				if (I[i] > max_I) {
					max_I = I[i];
				}

			}

			start_param[0] = max_I;

			for (int j = 0; j < ndims; j++) {
				start_param[j + 1] = X_sum[j] / I_sum;
			}

			for (int j = 0; j < ndims; j++) {
				double C = 0;
				double dx;
				for (int i = 0; i < X.length; i++) {
					dx = X[i][j] - start_param[j + 1];
					C += I[i] * dx * dx;
				}
				C /= I_sum;
				start_param[ndims + j + 1] = 1 / C;
			}
			start_param[2 * ndims + 1] = 0;
			start_param[2 * ndims + 2] = 0;

			return start_param;
		}

		// Get final parameters for the Gaussian fit along the line, input the
			// centroid position and value where the Gaussian has to be fitted
			// in the image
			public double[] Getfinalparam(final Localizable point) throws Exception {

				
				
				
				
				PointSampleList<FloatType> datalist = gatherData(point);

				final Cursor<FloatType> listcursor = datalist.localizingCursor();

				double[][] X = new double[(int) datalist.size()][ndims];
				double[] I = new double[(int) datalist.size()];
				int index = 0;
				while (listcursor.hasNext()) {
					listcursor.fwd();

					for (int d = 0; d < ndims; d++) {
						X[index][d] = listcursor.getDoublePosition(d);
					}

					I[index] = listcursor.get().getRealDouble();

					index++;
				}

				final double[] start_param = makeBestGuess(point, X, I);
				
				
				final double[] finalparam = start_param.clone();
				int maxiter = 500;
				double lambda = 1e-3;
				double termepsilon = 1e-1;

				LevenbergMarquardtSolverPoints.solve(X, finalparam, I, new TwoDimensionalGauss(), lambda, termepsilon, maxiter);

				// NaN protection: we prefer returning the crude estimate than NaN
				for (int j = 0; j < finalparam.length; j++) {
					if (Double.isNaN(finalparam[j]))
						finalparam[j] = start_param[j];
				}
				
				double[] sigma = {1.0 / Math.sqrt(finalparam[3]),1.0 / Math.sqrt(finalparam[4])  };
				for (int j = 0; j < sigma.length; j++) {
					if (Double.isNaN(sigma[j]))
						finalparam[j + ndims + 1] = start_param[j + ndims + 1];
				}
				


				return finalparam;
				

			}
			
			private PointSampleList<FloatType> gatherData(final Localizable point) {
				final PointSampleList<FloatType> datalist = new PointSampleList<FloatType>(ndims);

				

				final long[] minCorner = Watersheddding.GetMincorners(intimg, currentlabel);
				final long[] maxCorner = Watersheddding.GetMaxcorners(intimg, currentlabel);
				
				int radius = Math.min(30,(int) Distance(minCorner, maxCorner) / 8);
				
				RandomAccess<FloatType> ranac = inputimg.randomAccess();

				ranac.setPosition(point);

				RandomAccess<IntType> intranac = intimg.randomAccess();

				
				
				intranac.setPosition(point);

				final double[] position = new double[ndims];
				point.localize(position);

				// Gather data around the point
				boolean outofbounds = false;
				for (int d = 0; d < ndims; d++) {
					
					if (point.getDoublePosition(d) <= 0 || point.getDoublePosition(d)>= inputimg.dimension(d)){
						
						outofbounds = true;
						break;
					}
				}
				HyperSphere<FloatType> region = new HyperSphere<FloatType>(inputimg, point, radius);

				HyperSphereCursor<FloatType> localcursor = region.localizingCursor();

				final int label = intranac.get().get();

						
						
				while (localcursor.hasNext()) {
					localcursor.fwd();

					for (int d = 0; d < ndims; d++) {

						if (localcursor.getDoublePosition(d) < 0 || localcursor.getDoublePosition(d) >= inputimg.dimension(d)) {
							outofbounds = true;
							break;
						}
					}
					if (outofbounds) {
						outofbounds = false;
						continue;
					}
					
					intranac.setPosition(localcursor);
					int i = intranac.get().get();
					if (i == label) {

											
						
						Point newpoint = new Point(localcursor);
						datalist.add(newpoint, localcursor.get().copy());
						
							}
							
						}

					
				

				return datalist;
			}

			public double Distance(final long[] cordone, final long[] cordtwo) {

				double distance = 0;
				final double ndims = cordone.length;

				for (int d = 0; d < ndims; ++d) {

					distance += Math.pow((cordone[d] - cordtwo[d]), 2);

				}
				return Math.sqrt(distance);
			}

	}


