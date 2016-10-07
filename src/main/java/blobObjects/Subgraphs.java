package blobObjects;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import segmentBlobs.Staticproperties;

public class Subgraphs {

	
	
	public final int Previousframe;
	public final int Currentframe;
	public final SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> subgraph;
	
	public Subgraphs(final int Previousframe, final int Currentframe, final SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> subgraph  ){
		
		this.Previousframe = Previousframe;
		this.Currentframe = Currentframe;
		this.subgraph = subgraph;
		
	}
}
