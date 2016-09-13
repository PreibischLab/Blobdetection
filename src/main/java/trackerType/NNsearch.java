package trackerType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import graphconstructs.Logger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
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

	private final IntervalView<FloatType> baseframe;
	private final ArrayList<Staticproperties> Spotmaxbase;
	private final ArrayList<Staticproperties> Spotmaxtarget;
	private final IntervalView<FloatType> targetframe;
	private final double maxdistance;
	private final int ndims;
	private SimpleWeightedGraph< Staticproperties, DefaultWeightedEdge > graph;
	protected Logger logger = Logger.VOID_LOGGER;
	protected String errorMessage;

	public NNsearch(
			final IntervalView<FloatType> baseframe, 
			final IntervalView<FloatType> targetframe,
			final ArrayList<Staticproperties> Spotmaxbase,
			final ArrayList<Staticproperties> Spotmaxtarget,
			final double maxdistance){
		this.baseframe = baseframe;
		this.targetframe = targetframe;
		this.Spotmaxbase = Spotmaxbase;
		this.Spotmaxtarget = Spotmaxtarget;
		this.maxdistance = maxdistance;
		this.ndims = baseframe.numDimensions();
		
		assert baseframe.numDimensions() == targetframe.numDimensions();
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

	@Override
	public boolean process() {

		reset();
		
		

			
			
			final RealPointSampleList<Double> baselist = new RealPointSampleList<Double>(baseframe.numDimensions());
			
			final RandomAccess<FloatType> baseranac = baseframe.randomAccess();
			
			final double[] basepos = new double[ndims];
			
			for (int index = 0; index < Spotmaxbase.size(); ++index){
				RealPoint basepoint = new RealPoint(Spotmaxbase.get(index).location);
				
				Point intbasepoint = new Point(ndims);
				
				basepoint.localize(basepos);
				
				for (int d = 0; d < ndims ; ++d)
				intbasepoint.setPosition((long)basepos[d], d);
				
				baseranac.setPosition(intbasepoint);
					
				baselist.add(basepoint, baseranac.get().getRealDouble());
			}
			
	      final RealPointSampleList<Double> targetlist = new RealPointSampleList<Double>(targetframe.numDimensions());
	      final RandomAccess<FloatType> targetranac = baseframe.randomAccess();
			
		 final double[] targetpos = new double[ndims];
	      
			for (int index = 0; index < Spotmaxtarget.size(); ++index){
				RealPoint targetpoint = new RealPoint(Spotmaxtarget.get(index).location);
				
				Point inttargetpoint = new Point(ndims);
				
				targetpoint.localize(targetpos);
				
				for (int d = 0; d < ndims ; ++d)
				inttargetpoint.setPosition((long)basepos[d], d);
				
				targetranac.setPosition(inttargetpoint);
					
				targetlist.add(targetpoint,targetranac.get().getRealDouble() );
			}
			
			if (baselist != null && targetlist!=null){
			System.out.println(" Making KD Tree for NN search: ");
			
			final KDTree<Double> tree = new KDTree<Double>(targetlist);
			
			final NearestNeighborSearchOnKDTree<Double> search = new NearestNeighborSearchOnKDTree<Double>(tree);
			
			RealCursor<Double> listcursor = baselist.localizingCursor();
			
			while(listcursor.hasNext()){
				
				listcursor.fwd();
				
				search.search(listcursor);
				
				//System.out.println("Base frame point X: " + listcursor.getDoublePosition(0)+ " Y: " +  listcursor.getDoublePosition(1));
				//System.out.println("Nearest neighbour in target frame X: " + search.getPosition().getDoublePosition(0) +  " Y: " 
				//+ search.getPosition().getDoublePosition(1) );
				
				final double[] startpos = new double[ndims];
				final double[] endpos = new double[ndims];
				
				// Return a Nearest neighbour only if the distnace between particles in the frames is less than max distance
				
				if (search.getDistance()< maxdistance){
					
				listcursor.localize(startpos);
				search.getPosition().localize(endpos);
			
				
				
				}
			}
			System.out.println("NN detected, moving to next frame!");
		}
		
			
			logger.setStatus("Tracking...");
			logger.setProgress(0);


			logger.setProgress(1);
			logger.setStatus("");

			return true;
			
		}
	

	@Override
	public void setLogger( final Logger logger) {
		this.logger = logger;
		
	}
	
	public  void NearestNeighbour(ArrayList<double[]> tracklist){
		
		
		
		
		RandomAccessibleInterval<IntType> labelledimagebase = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());

		RandomAccessibleInterval<IntType> labelledimagetarget = new ArrayImgFactory<IntType>().create(baseframe,
				new IntType());
		// Find Maxima of blobs by segmenting the image via watershed
		labelledimagebase = segmentBlobs.Segmentbywatershed.getsegmentedimage(baseframe);
		labelledimagetarget = segmentBlobs.Segmentbywatershed.getsegmentedimage(targetframe);
		
		System.out.println("Segmenting base image and doing DoG detection:");
		// List containing all the maximas in baseframe
		ArrayList<Staticproperties> Spotmaxbase = segmentBlobs.Segmentbywatershed.DoGdetection(baseframe,
				labelledimagebase);
		System.out.println("Done!");
		System.out.println("Segmenting target image and doing DoG detection:");
		// List containing all the maximas in targetframe
		ArrayList<Staticproperties> Spotmaxtarget = segmentBlobs.Segmentbywatershed.DoGdetection(targetframe,
				labelledimagetarget);
		System.out.println("Done!");
		
		final RealPointSampleList<Double> baselist = new RealPointSampleList<Double>(baseframe.numDimensions());
		
		final RandomAccess<FloatType> baseranac = baseframe.randomAccess();
		
		final double[] basepos = new double[ndims];
		
		for (int index = 0; index < Spotmaxbase.size(); ++index){
			RealPoint basepoint = new RealPoint(Spotmaxbase.get(index).location);
			
			Point intbasepoint = new Point(ndims);
			
			basepoint.localize(basepos);
			
			for (int d = 0; d < ndims ; ++d)
			intbasepoint.setPosition((long)basepos[d], d);
			
			baseranac.setPosition(intbasepoint);
				
			baselist.add(basepoint, baseranac.get().getRealDouble());
		}
		
      final RealPointSampleList<Double> targetlist = new RealPointSampleList<Double>(targetframe.numDimensions());
      final RandomAccess<FloatType> targetranac = baseframe.randomAccess();
		
	 final double[] targetpos = new double[ndims];
      
		for (int index = 0; index < Spotmaxtarget.size(); ++index){
			RealPoint targetpoint = new RealPoint(Spotmaxtarget.get(index).location);
			
			Point inttargetpoint = new Point(ndims);
			
			targetpoint.localize(targetpos);
			
			for (int d = 0; d < ndims ; ++d)
			inttargetpoint.setPosition((long)basepos[d], d);
			
			targetranac.setPosition(inttargetpoint);
				
			targetlist.add(targetpoint,targetranac.get().getRealDouble() );
		}
		
		if (baselist != null && targetlist!=null){
		System.out.println(" Making KD Tree for NN search: ");
		
		final KDTree<Double> tree = new KDTree<Double>(targetlist);
		
		final NearestNeighborSearchOnKDTree<Double> search = new NearestNeighborSearchOnKDTree<Double>(tree);
		
		RealCursor<Double> listcursor = baselist.localizingCursor();
		
		while(listcursor.hasNext()){
			
			listcursor.fwd();
			
			search.search(listcursor);
			
			//System.out.println("Base frame point X: " + listcursor.getDoublePosition(0)+ " Y: " +  listcursor.getDoublePosition(1));
			//System.out.println("Nearest neighbour in target frame X: " + search.getPosition().getDoublePosition(0) +  " Y: " 
			//+ search.getPosition().getDoublePosition(1) );
			
			final double[] startpos = new double[ndims];
			final double[] endpos = new double[ndims];
			
			// Return a Nearest neighbour only if the distnace between particles in the frames is less than max distance
			
			if (search.getDistance()< maxdistance){
				
			listcursor.localize(startpos);
			search.getPosition().localize(endpos);
			final double[] track = { startpos[0], startpos[1],  endpos[0], endpos[1]};
			tracklist.add(track);
			
			
			}
		}
		System.out.println("NN detected, moving to next frame!");
	}
	
	}

	
	public void reset() {
		graph = new SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		final Iterator<Staticproperties> it = Spotmaxbase.iterator();
		while (it.hasNext()) {
			graph.addVertex(it.next());
		}
	}

	@Override
	public String getErrorMessage() {
		
		return errorMessage;
	}
}
