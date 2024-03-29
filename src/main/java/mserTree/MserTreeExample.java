package mserTree;
/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.EllipseRoi;
import ij.gui.Overlay;
import ij.process.ByteProcessor;

import java.awt.Color;
import java.io.File;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Example of computing and visualizing the {@link MserTree} of an image.
 *
 *
 * @author Tobias Pietzsch
 */
public class MserTreeExample< T extends IntegerType< T > >
{
	final ImagePlus imp;
	final Overlay ov;
	final ImageStack stack;
	final int w;
	final int h;

	public MserTreeExample( final ImagePlus imp, final ImageStack stack )
	{
		this.imp = imp;
		ov = new Overlay();
		imp.setOverlay( ov );
		this.stack = stack;
		this.w = imp.getWidth();
		this.h = imp.getHeight();
	}

	/**
	 * Visualise MSER. Add a 3sigma ellipse overlay to {@link #imp} in the given
	 * color. Add a slice to {@link #stack} showing binary mask of MSER region.
	 */
	public void visualise( final Mser< T > mser, final Color color )
	{
		final ByteProcessor byteProcessor = new ByteProcessor( w, h );
		final byte[] pixels = ( byte[] )byteProcessor.getPixels();
		for ( final Localizable l : mser )
		{
			final int x = l.getIntPosition( 0 );
			final int y = l.getIntPosition( 1 );
			pixels[ y * w + x ] = (byte)(255 & 0xff);
		}
		final String label = "" + mser.value();
		stack.addSlice( label, byteProcessor );

		final EllipseRoi ellipse = createEllipse( mser.mean(), mser.cov(), 3 );
		ellipse.setStrokeColor( color );
		ov.add( ellipse );
	}

	/**
	 * Visualize all MSER in a tree. {@see #visualise(Mser, Color)}.
	 */
	public void visualise( final MserTree< T > tree, final Color color )
	{
		for ( final Mser< T > mser : tree )
			visualise( mser, color );
	}

	/**
	 * Paint ellipse at nsigmas standard deviations
	 * of the given 2D Gaussian distribution.
	 *
	 * @param mean (x,y) components of mean vector
	 * @param cov (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public static EllipseRoi createEllipse( final double[] mean, final double[] cov, final double nsigmas )
	{
        final double a = cov[0];
        final double b = cov[1];
        final double c = cov[2];
        final double d = Math.sqrt( a*a + 4*b*b - 2*a*c + c*c );
        final double scale1 = Math.sqrt( 0.5 * ( a+c+d ) ) * nsigmas;
        final double scale2 = Math.sqrt( 0.5 * ( a+c-d ) ) * nsigmas;
        final double theta = 0.5 * Math.atan2( (2*b), (a-c) );
        final double x = mean[ 0 ];
        final double y = mean[ 1 ];
        final double dx = scale1 * Math.cos( theta );
        final double dy = scale1 * Math.sin( theta );
        final EllipseRoi ellipse = new EllipseRoi( x-dx, y-dy, x+dx, y+dy, scale2 / scale1 );
		return ellipse;
	}

	public static void main( final String[] args )
	{
		final int delta = 15;
		final long minSize = 10;
		final long maxSize = 100*100;
		final double maxVar = 0.8;
		final double minDiversity = 0;

		final Img< UnsignedByteType > img;
		final RandomAccessibleInterval<FloatType> newimp = util.ImgLib2Util.openAs32Bit(
				new File(
						"/Users/varunkapoor/Documents/Pierre_data/Recording_Cell_Culture_Kapoor/mCherry_ShortET-secdup-test.tif"),
				new ArrayImgFactory<FloatType>());
		
		new Normalize();

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(newimp), minval, maxval);
		int ndims = newimp.numDimensions();
		
		IntervalView<FloatType> newimg = Views.hyperSlice(newimp, ndims - 1, 0);
		ImageJFunctions.show(newimg);
		try
		{
			new ImageJ();
			
			
			final ImagePlus currentimp = IJ.getImage();
			IJ.run("Lena (68K)");
			IJ.run("8-bit");
			
			img = ImagePlusAdapter.wrapByte( currentimp );
			ImageJFunctions.show(img);
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			return;
		}
		final ImagePlus impImg = IJ.getImage();
		final ImageStack stack = new ImageStack( (int) img.dimension( 0 ), (int) img.dimension( 1 ) );

		final MserTree< UnsignedByteType > treeDarkToBright = MserTree.buildMserTree( img, new UnsignedByteType( delta ), minSize, maxSize, maxVar, minDiversity, true );
		final MserTree< UnsignedByteType > treeBrightToDark = MserTree.buildMserTree( img, new UnsignedByteType( delta ), minSize, maxSize, maxVar, minDiversity, false );
		final MserTreeExample< UnsignedByteType > vis = new MserTreeExample< UnsignedByteType >( impImg, stack );
		vis.visualise( treeDarkToBright, Color.GREEN );
		vis.visualise( treeBrightToDark, Color.RED );

		final ImagePlus imp = new ImagePlus("components", stack);
		imp.show();
	}
}