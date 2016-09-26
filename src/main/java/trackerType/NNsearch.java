package trackerType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import graphconstructs.Logger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
import kdTreeBlobs.FlagNode;
import kdTreeBlobs.NNFlagsearchKDtree;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealCursor;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import segmentBlobs.Staticproperties;

public class NNsearch implements Blob {

	private final ArrayList<ArrayList<Staticproperties>> Allblobs;
	private final double maxdistance;
	private final long maxframe;
	private SimpleWeightedGraph< Staticproperties, DefaultWeightedEdge > graph;
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;

	public NNsearch(
			final ArrayList<ArrayList<Staticproperties>> Allblobs, final double maxdistance, 
			final long maxframe){
		this.Allblobs = Allblobs;
		this.maxdistance = maxdistance;
		this.maxframe = maxframe;
		
		
	}
	
	
	
	

	@Override
	public boolean process() {

		reset();
		
		
		for (int frame = 0; frame < maxframe - 1; ++frame){
		
		
			ArrayList<Staticproperties> Spotmaxbase = Allblobs.get(frame);
			
			ArrayList<Staticproperties> Spotmaxtarget = Allblobs.get(frame + 1);
			
			Iterator<Staticproperties> baseobjectiterator = Spotmaxbase.iterator();
			
			
			
	        final int Targetblobs = Spotmaxtarget.size();
	        
			final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Targetblobs);

			final List<FlagNode<Staticproperties>> targetNodes = new ArrayList<FlagNode<Staticproperties>>(Targetblobs);
			
			
	      
			for (int index = 0; index < Spotmaxtarget.size(); ++index){
				
				
				
				targetCoords.add(new RealPoint(Spotmaxtarget.get(index).location));

				targetNodes.add(new FlagNode<Staticproperties>(Spotmaxtarget.get(index)));
				
				
			}
			
			if (targetNodes.size() > 0 && targetCoords.size() > 0){
			
			final KDTree<FlagNode<Staticproperties>> Tree = new KDTree<FlagNode<Staticproperties>>(targetNodes, targetCoords);
			
			final NNFlagsearchKDtree<Staticproperties> Search = new NNFlagsearchKDtree<Staticproperties>(Tree);
			
			System.out.println(" Making KD Tree for NN search: ");
			
			
			while(baseobjectiterator.hasNext()){
				
				final Staticproperties source = baseobjectiterator.next();
				final RealPoint sourceCoords = new RealPoint(source.location);
				Search.search(sourceCoords);
				final double squareDist = Search.getSquareDistance();
				final FlagNode<Staticproperties> targetNode = Search.getSampler().get();
				if (squareDist > maxdistance)
					continue;

				targetNode.setVisited(true);
				
				synchronized (graph) {
					
					graph.addVertex(source);
					graph.addVertex(targetNode.getValue());
					final DefaultWeightedEdge edge = graph.addEdge(source, targetNode.getValue());
					graph.setEdgeWeight(edge, squareDist);
					
					
				}
			
		       
			}
			
			System.out.println("NN detected, moving to next frame!");
		}
		}
		
			return true;
			
		}
	

	@Override
	public void setLogger( final Logger logger) {
		this.logger = logger;
		
	}
	

	@Override
	public SimpleWeightedGraph< Staticproperties, DefaultWeightedEdge > getResult()
	{
		return graph;
	}
	
	@Override
	public boolean checkInput() {
		final StringBuilder errrorHolder = new StringBuilder();;
		final boolean ok = checkInput();
		if (!ok) {
			errorMessage = errrorHolder.toString();
		}
		return ok;
	}
	
	public void reset() {
		graph = new SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		final Iterator<Staticproperties> it = Allblobs.get(0).iterator();
		while (it.hasNext()) {
			graph.addVertex(it.next());
		}
	}

	@Override
	public String getErrorMessage() {
		
		return errorMessage;
	}
}
