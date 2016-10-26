package mserTree;

import java.awt.Color;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.EllipseRoi;
import ij.gui.Overlay;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
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

public class GetMSERtree < T extends IntegerType< T > > {

	
	final ImagePlus imp;
	final Overlay ov;
	final ImageStack stack;
	final int w;
	final int h;
	
	
	public GetMSERtree( final ImagePlus imp, final ImageStack stack )
	{
		this.imp = imp;
		ov = new Overlay();
		imp.setOverlay( ov );
		
		this.stack = stack;
		this.w = imp.getWidth();
		this.h = imp.getHeight();
		
	}
	
	
	public static < T extends RealType<T>>  RandomAccessibleInterval<UnsignedByteType> Typeconversion (RandomAccessibleInterval<T> inputimg){
		
		 
		ImageJFunctions.show(inputimg);
				final ImagePlus currentimp = IJ.getImage();
				IJ.run("Lena (68K)");
				IJ.run("8-bit");
				
				final RandomAccessibleInterval<UnsignedByteType> output  = ImagePlusAdapter.wrapByte( currentimp );
		
		/*= Converters.convert(
				 inputimg,
				 new Converter<T, UnsignedByteType>(){

					@Override
					
					public void convert(T input, UnsignedByteType output) {
						output.setReal(input.getRealDouble()*1000);
						
					}
					 
				 },
				 new UnsignedByteType());
		*/
		return output;
	}
	
	public static < T extends RealType<T>> MserTree<UnsignedByteType>  Treeimage(Img< UnsignedByteType > inputimg, final double delta, final long minSize, final long maxSize, final double maxVar, final double minDiversity, final boolean darkToBright ){
		
		
		//final RandomAccessibleInterval<UnsignedByteType> mserInput = Typeconversion(inputimg);
		
		 MserTree<UnsignedByteType> newtree =  MserTree.buildMserTree(inputimg,  delta, minSize, maxSize, maxVar, minDiversity, darkToBright);
		 
		return newtree;
		
	}
	
	public <T extends RealType<T>> void visualise( final MserTree< T > tree, final Color color )
	{
		for ( final Mser< T > mser : tree )
			visualise( mser, color );
	}
	
	/**
	 * Visualise MSER. Add a 3sigma ellipse overlay to imgplus in the given
	 * color. Add a slice to the stack showing binary mask of MSER region.
	 */
	public <T extends RealType<T>> void visualise( final Mser< T > mser, final Color color )
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
	

	 /** 2D correlated Gaussian
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
	
	
	public static  void MaxProjection(ImagePlus imp){
		
		Img<FloatType> img3D, img2D;
		
		final ImgFactory factory = new ArrayImgFactory<FloatType>();
		
		img3D = factory.create( new int[]{ imp.getWidth(), imp.getHeight(), imp.getStack().getSize() }, new FloatType() );
		img2D = factory.create( new int[]{ imp.getWidth(), imp.getHeight() }, new FloatType() );
		
		
		final Cursor< FloatType > cursor = img3D.localizingCursor();
		final RandomAccess<FloatType> ranac2D = img2D.randomAccess();
		
		final double maxinZ = img3D.max(imp.getNDimensions() - 1);
		
		final ArrayList< ImageProcessor > ips = new ArrayList< ImageProcessor >();

		for ( int z = 0; z < imp.getStack().getSize(); ++z )
			ips.add( imp.getStack().getProcessor( z + 1 ) );
		
		while(cursor.hasNext()){
			
			cursor.fwd();
			
			final int x = cursor.getIntPosition(0);
			final int y = cursor.getIntPosition(1);
			final int z = cursor.getIntPosition(2);
			
			cursor.get().set(ips.get(z).getf(x,y));
			
			ranac2D.setPosition(x, 0);
			ranac2D.setPosition(y, 1);
			
			ranac2D.get().setReal(maxinZ);
			
		}
		
		
		
	}
	
	
}
