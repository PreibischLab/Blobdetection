package mserTree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.EllipseRoi;
import ij.gui.Overlay;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponent;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponentTree;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

public class GetMSERtree<T extends RealType<T>> {

	final ImagePlus imp;
	final Overlay ov;
	final ImageStack stack;
	final int w;
	final int h;

	public GetMSERtree(final ImagePlus imp, final ImageStack stack) {
		this.imp = imp;
		ov = new Overlay();
		imp.setOverlay(ov);

		this.stack = stack;
		this.w = imp.getWidth();
		this.h = imp.getHeight();

	}

	public GetMSERtree(final ImagePlus imp) {

		this.imp = imp;
		ov = new Overlay();
		imp.setOverlay(ov);

		this.stack = null;
		this.w = imp.getWidth();
		this.h = imp.getHeight();
	}

	public static <T extends RealType<T>> MserTree<UnsignedByteType> Treeimage(Img<UnsignedByteType> inputimg,
			final double delta, final long minSize, final long maxSize, final double maxVar, final double minDiversity,
			final boolean darkToBright) {

		MserTree<UnsignedByteType> newtree = MserTree.buildMserTree(inputimg, delta, minSize, maxSize, maxVar,
				minDiversity, darkToBright);

		return newtree;

	}

	public void visualise(final MserTree<T> tree, final Color color) {

		ArrayList<double[]> meanandcovlist = new ArrayList<double[]>();
		ArrayList<double[]> redmeanandcovlist = new ArrayList<double[]>();
		ArrayList<double[]> meanandcovchildlist = new ArrayList<double[]>();
		final HashSet<Mser<T>> rootset = tree.roots();

		final Iterator<Mser<T>> rootsetiterator = rootset.iterator();
		final Iterator<Mser<T>> treeiterator = tree.iterator();
		System.out.println("Size of root set: " + rootset.size());

		while (rootsetiterator.hasNext()) {

			Mser<T> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1], rootmser.cov()[0],
						rootmser.cov()[1], rootmser.cov()[2] };

				meanandcovlist.add(meanandcov);

			}

		}
		while (treeiterator.hasNext()) {

			Mser<T> mser = treeiterator.next();

			if (mser.getChildren().size() - 1 > 0) {

				for (int index = 0; index < mser.getChildren().size(); ++index) {

					final double[] meanandcovchild = { mser.getChildren().get(index).mean()[0],
							mser.getChildren().get(index).mean()[1], mser.getChildren().get(index).cov()[0],
							mser.getChildren().get(index).cov()[1], mser.getChildren().get(index).cov()[2] };

					meanandcovchildlist.add(meanandcovchild);

				}

			}

		}
		
		redmeanandcovlist = meanandcovlist;
		for (int childindex = 0; childindex < meanandcovchildlist.size(); ++childindex) {
			
			final double[] meanchild = new double[] { meanandcovchildlist.get(childindex)[0], meanandcovchildlist.get(childindex)[1] };
			final double[] covarchild = new double[] { meanandcovchildlist.get(childindex)[2], meanandcovchildlist.get(childindex)[3],
					meanandcovchildlist.get(childindex)[4] };
			final EllipseRoi ellipsechild = createEllipse(meanchild, covarchild, 3);
			
				ellipsechild.setStrokeColor(color);
				ov.add(ellipsechild);
			
	
		
		

		for (int index = 0; index < meanandcovlist.size(); ++index) {

			final double[] mean = new double[] { meanandcovlist.get(index)[0], meanandcovlist.get(index)[1] };
			final double[] covar = new double[] { meanandcovlist.get(index)[2], meanandcovlist.get(index)[3],
					meanandcovlist.get(index)[4] };
			final EllipseRoi ellipse = createEllipse(mean, covar, 3);
			
			
			if (ellipse.contains((int) meanchild[0], (int) meanchild[1]) )
				redmeanandcovlist.remove(index);
				
		}
		

		}
		
		for (int index = 0; index < redmeanandcovlist.size(); ++index) {

			final double[] mean = new double[] { redmeanandcovlist.get(index)[0], redmeanandcovlist.get(index)[1] };
			final double[] covar = new double[] { redmeanandcovlist.get(index)[2], redmeanandcovlist.get(index)[3],
					redmeanandcovlist.get(index)[4] };
			final EllipseRoi ellipse = createEllipse(mean, covar, 3);
		ellipse.setStrokeColor(color);

			ov.add(ellipse);
	
		}

	}

	public void visualise(final PixelListComponentTree<T> tree) {
		for (final PixelListComponent<T> pixel : tree)
			visualise(pixel);
	}

	/**
	 * Visualise PixelListComponentTree.
	 */
	public void visualise(final PixelListComponent<T> pixel) {

		final ByteProcessor byteProcessor = new ByteProcessor(w, h);
		final byte[] pixels = (byte[]) byteProcessor.getPixels();
		System.out.println(pixel.value());
		for (final Localizable l : pixel) {
			final int x = l.getIntPosition(0);
			final int y = l.getIntPosition(1);
			pixels[y * w + x] = (byte) (255 & 0xff);

		}
		final String label = "" + pixel.value();
		if (stack != null)
			stack.addSlice(label, byteProcessor);

	}

	/**
	 * Visualise MSER. Add a 3sigma ellipse overlay to imgplus in the given
	 * color. Add a slice to the stack showing binary mask of MSER region.
	 */
	public void visualise(final Mser<T> mser, final Color color) {

		final ByteProcessor byteProcessor = new ByteProcessor(w, h);
		final byte[] pixels = (byte[]) byteProcessor.getPixels();
		for (final Localizable l : mser) {
			final int x = l.getIntPosition(0);
			final int y = l.getIntPosition(1);
			pixels[y * w + x] = (byte) (255 & 0xff);

		}
		final String label = "" + mser.value();
		if (stack != null)
			stack.addSlice(label, byteProcessor);

	}

	/**
	 * 2D correlated Gaussian
	 * 
	 * @param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public static EllipseRoi createEllipse(final double[] mean, final double[] cov, final double nsigmas) {
		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);
		final double scale1 = Math.sqrt(0.5 * (a + c + d)) * nsigmas;
		final double scale2 = Math.sqrt(0.5 * (a + c - d)) * nsigmas;
		final double theta = 0.5 * Math.atan2((2 * b), (a - c));
		final double x = mean[0];
		final double y = mean[1];
		final double dx = scale1 * Math.cos(theta);
		final double dy = scale1 * Math.sin(theta);
		final EllipseRoi ellipse = new EllipseRoi(x - dx, y - dy, x + dx, y + dy, scale2 / scale1);
		return ellipse;
	}

	public static <T extends RealType<T>> SortedSet<Double> MseratThreshold(MserTree<T> tree) {

		SortedSet<Double> MseratT = new TreeSet<Double>();
		for (final Mser<T> mser : tree) {

			MseratT.add(mser.value().getRealDouble());
		}

		return MseratT;
	}

	public static <T extends RealType<T>> Mser<T> CovMatDet(MserTree<T> tree) {

		double minDet = Double.MAX_VALUE;
		Mser<T> mostStable = null;

		for (final Mser<T> mser : tree) {

			final double CovXX = mser.cov()[0];
			final double CovXY = mser.cov()[1];
			final double CovYY = mser.cov()[2];
			final double detCovar = CovXX * CovYY - CovXY * CovXY;

			if (detCovar < minDet) {

				minDet = detCovar;
				mostStable = mser;

			}

		}

		return mostStable;
	}

	public static RandomAccessibleInterval<FloatType> MaxProjection(ImagePlus imp) {
		Img<FloatType> img3D, img2D;

		final ImgFactory<FloatType> factory = new ArrayImgFactory<FloatType>();

		int height = imp.getHeight();
		int width = imp.getWidth();

		img3D = factory.create(new int[] { width, height, imp.getStack().getSize() }, new FloatType());
		img2D = factory.create(new int[] { width, height }, new FloatType());

		img3D = ImagePlusAdapter.wrapFloat(imp);

		final RandomAccess<FloatType> ranac2D = img2D.randomAccess();

		final ArrayList<ImageProcessor> ips = new ArrayList<ImageProcessor>();
		int maxZ = (int) img3D.dimension(2);
		int minZ = (int) img3D.min(2);
		for (int z = 0; z < imp.getStack().getSize(); ++z)
			ips.add(imp.getStack().getProcessor(z + 1));

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				final ArrayList<Float> IntensityAlongZ = new ArrayList<Float>();

				for (int index = minZ; index < maxZ; ++index) {

					final float Zintensity = ips.get(index).getf(x, y);

					IntensityAlongZ.add(Zintensity);

				}

				Collections.sort(IntensityAlongZ);
				ranac2D.setPosition(new int[] { x, y });
				ranac2D.get().set(IntensityAlongZ.get(0));

			}

		}

		return img2D;

	}

}
