package mserTree;

import java.io.File;

import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;

public class Exampleprojection {

	
	public static void main( final String[] args ){
		
		new ImageJ();

		// Load the stack of images
		final RandomAccessibleInterval<FloatType> img = util.ImgLib2Util.openAs32Bit(
				new File(
						"../res/first_frame_MSER.tif"),
				new ArrayImgFactory<FloatType>());
		
		
		
		ImagePlus imp = ImageJFunctions.show(img);
		
		RandomAccessibleInterval<FloatType> maxprojection = GetMSERtree.MaxProjection(imp);
		
		ImageJFunctions.show(maxprojection);
		
		
		
	}
	
	
}
