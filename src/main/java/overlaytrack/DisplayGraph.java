package overlaytrack;

import java.awt.Color;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import segmentBlobs.Staticproperties;

public class DisplayGraph {

	public static void displaytracks(RandomAccessibleInterval<FloatType> imgout,
			SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph){
		
		ImageStack stack = new ImageStack((int) imgout.dimension(0), (int) imgout.dimension(1));

		stack.addSlice(ImageJFunctions.wrap(imgout, "").getProcessor());

		ImagePlus imp = new ImagePlus("Detected tracks", stack);
		imp.show();

		Overlay o = imp.getOverlay();

		if (o == null) {
			o = new Overlay();
			imp.setOverlay(o);
		}
		final int ndims = imgout.numDimensions();
		
		for (DefaultWeightedEdge e : graph.edgeSet()) {
	        Staticproperties Spotbase = graph.getEdgeSource(e);
	        Staticproperties Spottarget = graph.getEdgeTarget(e);
	        
	        final double[] startedge = new double[ndims];
	        final double[] targetedge = new double[ndims];
	        for (int d = 0; d < ndims; ++d){
	        	
	        	startedge[d] = Spotbase.location[d];
	        	
	        	targetedge[d] = Spottarget.location[d];
	        	
	        }
	        
	        
	        
	        
	        Line newline = new Line(startedge[0], startedge[1], targetedge[0], targetedge[1]);
			newline.setStrokeColor(Color.GREEN);
			newline.setStrokeWidth(0.8);

			o.add(newline);
			
			
			
	        
	        
	        
		}
		
		imp.updateAndDraw();
		
	}
	
	
	public static void Drawshortline(RandomAccessibleInterval<FloatType> imgout,final double[] startpos, final double[] endpos) {
		final int ndims = imgout.numDimensions();
		final double[] tmppos = new double[ndims];
		final double[] startline = new double[ndims];
		final double[] endline = new double[ndims];
		final double[] minVal = new double[ndims];
		final double[] maxVal = new double[ndims];
		for (int d = 0; d < ndims; ++d) {

			final double locationdiff = startpos[d] - endpos[d];
			final boolean minsearch = locationdiff > 0;
			tmppos[d] = startpos[d];

			
				minVal[d] = minsearch ? endpos[d] : startpos[d];
				maxVal[d] = minsearch ? tmppos[d] : endpos[d];
			
			}

		final double slope = ( maxVal[1] - minVal[1] )/ (maxVal[0] - minVal[0] );
		final double[] sigma = {1,1};
		
		if (slope >= 0) {
			for (int d = 0; d < ndims; ++d) {

				startline[d] = minVal[d];
				endline[d] = maxVal[d];
			}

		}

		if (slope < 0) {

			startline[0] = minVal[0];
			startline[1] = maxVal[1];
			endline[0] = maxVal[0];
			endline[1] = minVal[1];

		}
		
		
		
		
		
		final double stepsize = 1;
       double steppos[] = {startline[0], startline[1]};
		while (true) {

			steppos[0] += stepsize / Math.sqrt(1 + slope * slope);
			steppos[1] += stepsize * slope / Math.sqrt(1 + slope * slope);

			AddGaussian.addGaussian(imgout, steppos, sigma);

			if (steppos[0] >= endline[0] || steppos[1] >= endline[1] && slope > 0)
				break;

			if (steppos[0] >= endline[0] || steppos[1] <= endline[1] && slope < 0)
				break;

		}

	

	}
	
}
