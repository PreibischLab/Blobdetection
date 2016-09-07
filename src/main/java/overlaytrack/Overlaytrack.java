package overlaytrack;

import java.awt.Color;
import java.util.ArrayList;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

public class Overlaytrack {

	
	public static void Overlaynearest(IntervalView<FloatType> targetframe, final ArrayList<double[]> tracklist){
		
		ImageStack stack = new ImageStack((int) targetframe.dimension(0), (int) targetframe.dimension(1));

		stack.addSlice(ImageJFunctions.wrap(targetframe, "").getProcessor());

		ImagePlus imp = new ImagePlus("Detected tracks", stack);
		imp.show();

		Overlay o = imp.getOverlay();

		if (o == null) {
			o = new Overlay();
			imp.setOverlay(o);
		}

		//o.clear();
		for (int index = 0; index < tracklist.size(); ++index){
		Line newline = new Line(tracklist.get(index)[0], tracklist.get(index)[1], tracklist.get(index)[2], tracklist.get(index)[3]);
		newline.setStrokeColor(Color.GREEN);
		newline.setStrokeWidth(0.8);

		o.add(newline);
		}
		imp.updateAndDraw();
		
	}
	
}
