package overlaytrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blobObjects.FramedBlob;
import fakeblobs.AddGaussian;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.view.HyperSliceImgPlus;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import segmentBlobs.Staticproperties;

public class DisplayBlobs {

	
	/***
	 * 
	 * 
	 * @param img
	 * @param frameandblob
	 */
	public static void Displaydetection(RandomAccessibleInterval<FloatType> img, ArrayList<FramedBlob> frameandblob) {


		int ndims = img.numDimensions();

			

			for (int index = 0; index < frameandblob.size(); ++index) {

				int currentframe = frameandblob.get(index).frame;

				if (frameandblob.get(index).frame == currentframe ) {
					IntervalView<FloatType> groundframe = Views.hyperSlice(img, ndims - 1, currentframe);
					AddGaussian.addGaussian(groundframe, frameandblob.get(index).Blobs.Intensity,
							frameandblob.get(index).Blobs.location,
							new double[] { 2 *frameandblob.get(index).Blobs.maxextent ,
									2 *frameandblob.get(index).Blobs.maxextent  });

				}

			

		}

	}
	
	
	public static void DisplayRefineddetection(RandomAccessibleInterval<FloatType> img, ArrayList<FramedBlob> frameandblob) {


		int ndims = img.numDimensions();

			

			for (int index = 0; index < frameandblob.size(); ++index) {

				int currentframe = frameandblob.get(index).frame;

				if (frameandblob.get(index).frame == currentframe ) {
					IntervalView<FloatType> groundframe = Views.hyperSlice(img, ndims - 1, currentframe);
					AddGaussian.addGaussian(groundframe, frameandblob.get(index).Blobs.Intensity,
							frameandblob.get(index).Blobs.location,
							new double[] { frameandblob.get(index).Blobs.sigma[0] ,
									frameandblob.get(index).Blobs.sigma[1]  });
					
System.out.println(frameandblob.get(index).Blobs.sigma[0] + " " + frameandblob.get(index).Blobs.sigma[1]);
				}

			

		}

	}
	
	

}
