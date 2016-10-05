package overlaytrack;

import java.util.ArrayList;

import blobObjects.FramedBlob;
import fakeblobs.AddGaussian;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.view.HyperSliceImgPlus;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class DisplayBlobs {

	public static void Displaydetection(RandomAccessibleInterval<FloatType> img, ArrayList<FramedBlob> frameandblob) {

		int finalframe = frameandblob.get(frameandblob.size() - 1).frame;

		int ndims = img.numDimensions();

		for (int frame = 0; frame < finalframe ; ++frame) {
			IntervalView<FloatType> groundframe = Views.hyperSlice(img, ndims - 1, frame);

			for (int index = 0; index < frameandblob.size(); ++index) {

				int currentframe = frameandblob.get(index).frame;

				if (frame == currentframe) {

					AddGaussian.addGaussian(groundframe, frameandblob.get(index).Blobs.Intensity,
							frameandblob.get(index).Blobs.location,
							new double[] { frameandblob.get(index).Blobs.maxextent/2 ,
									frameandblob.get(index).Blobs.maxextent/2  });

				}

			}

		}

	}

}
