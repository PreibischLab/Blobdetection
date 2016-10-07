package overlaytrack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
import net.imagej.DrawingTool;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import segmentBlobs.Staticproperties;

public class DisplayGraph {
	// add listener to the imageplus slice slider
	private ImagePlus imp;
	private final SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph;
	private final int ndims;
	
	public DisplayGraph(final ImagePlus imp, SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph, final int ndims){
		
		this.imp = imp;
		this.graph = graph;
		this.ndims = ndims;
		
		// add listener to the imageplus slice slider
				SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
	}
	
	
	public ImagePlus getImp() { return this.imp; } 
	
	
	protected  class ImagePlusListener implements SliceListener
	{
		@Override
		public void sliceChanged(ImagePlus arg0)
		{
			
			
			imp.show();
			Overlay o = imp.getOverlay();
			
			if( getImp().getOverlay() == null )
			{
				o = new Overlay();
				getImp().setOverlay( o ); 
			}

			o.clear();
			getImp().getOverlay().clear(); 
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
				newline.setStrokeWidth(graph.degreeOf(Spottarget));

				o.add(newline);
				
			}
			imp.updateAndDraw();
			System.out.println( arg0.getCurrentSlice() );
		}		
	}
	
}
