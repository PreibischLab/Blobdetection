package trackerType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import net.imglib2.RealPoint;
import java.util.HashSet;
import net.imglib2.algorithm.Benchmark;
import overlaytrack.DisplayGraph;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobObjects.FramedBlob;
import blobObjects.Subgraphs;
import costMatrix.CostFunction;
import costMatrix.IntensityDiffCostFunction;
import costMatrix.JaqamanLinkingCostMatrixCreator;
import costMatrix.SquareDistCostFunction;
import graphconstructs.JaqamanLinker;
import graphconstructs.Logger;
import ij.ImagePlus;
import segmentBlobs.Staticproperties;

public class KFSearchRefined implements Blob {

	private static final double ALTERNATIVE_COST_FACTOR = 1.05d;

	private static final double PERCENTILE = 1d;

	private static final String BASE_ERROR_MSG = "[KalmanTracker] ";

	private final ArrayList<ArrayList<Staticproperties>> Allblobs;

	private final double maxsearchRadius;
	private final double initialsearchRadius;
	private final CostFunction<Staticproperties, Staticproperties> UserchosenCostFunction;
	private final int maxframe;
	private final int maxframeGap;

	private SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> graph;
	private ArrayList<FramedBlob> Allmeasured;
	private ArrayList<Subgraphs> Framedgraph;

	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;

	public KFSearchRefined(final ArrayList<ArrayList<Staticproperties>> Allblobs,
			final CostFunction<Staticproperties, Staticproperties> UserchosenCostFunction, final double maxsearchRadius,
			final double initialsearchRadius, final int maxframe, final int maxframeGap) {
		this.Allblobs = Allblobs;
		this.UserchosenCostFunction = UserchosenCostFunction;
		this.initialsearchRadius = initialsearchRadius;
		this.maxsearchRadius = maxsearchRadius;
		this.maxframe = maxframe;
		this.maxframeGap = maxframeGap;

	}

	@Override
	public SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> getResult() {
		return graph;
	}

	public ArrayList<FramedBlob> getFramelist() {

		return Allmeasured;
	}

	public ArrayList<Subgraphs> getFramedgraph() {

		return Framedgraph;
	}

	@Override
	public boolean checkInput() {
		return true;
	}

	@Override
	public boolean process() {

		/*
		 * Outputs
		 */

		graph = new SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Allmeasured = new ArrayList<FramedBlob>();
		Framedgraph = new ArrayList<Subgraphs>();

		// Find first two non-zero frames containing blobs

		int Firstframe = 0;
		int Secondframe = 0;

		for (int frame = 0; frame < maxframe; ++frame) {

			if (Allblobs.get(frame).size() > 0) {
				Firstframe = frame;
				break;
			}
		}

		for (int frame = Firstframe + 1; frame < maxframe; ++frame) {

			if (Allblobs.get(frame).size() > 0) {
				Secondframe = frame;
				break;
			}
		}

		Collection<Staticproperties> Firstorphan = Allblobs.get(Firstframe);

		Collection<Staticproperties> Secondorphan = Allblobs.get(Secondframe);

		// Max KF search cost.
		final double maxCost = maxsearchRadius * maxsearchRadius;

		// Max cost to nucleate KFs.
		final double maxInitialCost = initialsearchRadius * initialsearchRadius;

		/*
		 * Estimate Kalman filter variances.
		 *
		 * The search radius is used to derive an estimate of the noise that
		 * affects position and velocity. The two are linked: if we need a large
		 * search radius, then the fluoctuations over predicted states are
		 * large.
		 */
		final double positionProcessStd = maxsearchRadius / 2d;
		final double velocityProcessStd = maxsearchRadius / 2d;

		
		double meanSpotRadius = 0d;
		for (final Staticproperties Blob : Secondorphan) {
			
			meanSpotRadius += Math.max(Blob.sigma[0], Blob.sigma[1]);
		}
		meanSpotRadius /= Secondorphan.size();
		final double positionMeasurementStd = meanSpotRadius / 10d;

		final Map<CVMKalmanFilter, Staticproperties> kalmanFiltersMap = new HashMap<CVMKalmanFilter, Staticproperties>(
				Secondorphan.size());

		// Loop from the second frame to the last frame and build
		// KalmanFilterMap

		for (int frame = Secondframe; frame < maxframe; ++frame) {

			SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge> subgraph = new SimpleWeightedGraph<Staticproperties, DefaultWeightedEdge>(
					DefaultWeightedEdge.class);

			List<Staticproperties> measurements = Allblobs.get(frame);

			System.out.println("Doing KF search in frame number: " + frame + " " + "Number of blobs:"
					+ Allblobs.get(frame).size());

			// Make the preditiction map
			final Map<ComparableRealPoint, CVMKalmanFilter> predictionMap = new HashMap<ComparableRealPoint, CVMKalmanFilter>(
					kalmanFiltersMap.size());

			for (final CVMKalmanFilter kf : kalmanFiltersMap.keySet()) {
				final double[] X = kf.predict();
				final ComparableRealPoint point = new ComparableRealPoint(X);
				predictionMap.put(point, kf);

			}
			final List<ComparableRealPoint> predictions = new ArrayList<ComparableRealPoint>(predictionMap.keySet());

			// Orphans are dealt with later
			final Collection<CVMKalmanFilter> childlessKFs = new HashSet<CVMKalmanFilter>(kalmanFiltersMap.keySet());

			/*
			 * Here we simply link based on minimizing the squared distances to
			 * get an initial starting point, more advanced Kalman filter costs
			 * will be built in the next step
			 */

			if (!predictions.isEmpty() && !measurements.isEmpty()) {
				// Only link measurements to predictions if we have predictions.

				final JaqamanLinkingCostMatrixCreator<ComparableRealPoint, Staticproperties> crm = new JaqamanLinkingCostMatrixCreator<ComparableRealPoint, Staticproperties>(
						predictions, measurements, DistanceBasedcost, maxCost, ALTERNATIVE_COST_FACTOR, PERCENTILE);

				final JaqamanLinker<ComparableRealPoint, Staticproperties> linker = new JaqamanLinker<ComparableRealPoint, Staticproperties>(
						crm);
				if (!linker.checkInput() || !linker.process()) {
					errorMessage = BASE_ERROR_MSG + "Error linking candidates in frame " + frame + ": "
							+ linker.getErrorMessage();
					return false;
				}
				final Map<ComparableRealPoint, Staticproperties> agnts = linker.getResult();
				final Map<ComparableRealPoint, Double> costs = linker.getAssignmentCosts();

				// Deal with found links.
				Secondorphan = new HashSet<Staticproperties>(measurements);
				for (final ComparableRealPoint cm : agnts.keySet()) {
					final CVMKalmanFilter kf = predictionMap.get(cm);

					// Create links for found match.
					final Staticproperties source = kalmanFiltersMap.get(kf);
					final Staticproperties target = agnts.get(cm);

					graph.addVertex(source);
					graph.addVertex(target);
					final DefaultWeightedEdge edge = graph.addEdge(source, target);
					final double cost = costs.get(cm);
					graph.setEdgeWeight(edge, cost);

					subgraph.addVertex(source);
					subgraph.addVertex(target);
					final DefaultWeightedEdge subedge = subgraph.addEdge(source, target);
					subgraph.setEdgeWeight(subedge, cost);

					Subgraphs currentframegraph = new Subgraphs(frame - 1, frame, subgraph);

					Framedgraph.add(currentframegraph);
					final FramedBlob prevframedBlob = new FramedBlob(frame - 1, source);

					Allmeasured.add(prevframedBlob);

					final FramedBlob newframedBlob = new FramedBlob(frame, target);

					Allmeasured.add(newframedBlob);

					// Update Kalman filter
					kf.update(MeasureBlob(target));

					// Update Kalman track Staticproperties
					kalmanFiltersMap.put(kf, target);

					// Remove from orphan set
					Secondorphan.remove(target);

					// Remove from childless KF set
					childlessKFs.remove(kf);
				}
			}

			// Deal with orphans from the previous frame.
			// Here is the real linking with the actual cost function

			if (!Firstorphan.isEmpty() && !Secondorphan.isEmpty()) {

				// Trying to link orphans with unlinked candidates.

				final JaqamanLinkingCostMatrixCreator<Staticproperties, Staticproperties> ic = new JaqamanLinkingCostMatrixCreator<Staticproperties, Staticproperties>(
						Firstorphan, Secondorphan, UserchosenCostFunction, maxInitialCost, ALTERNATIVE_COST_FACTOR,
						PERCENTILE);
				final JaqamanLinker<Staticproperties, Staticproperties> newLinker = new JaqamanLinker<Staticproperties, Staticproperties>(
						ic);
				if (!newLinker.checkInput() || !newLinker.process()) {
					errorMessage = BASE_ERROR_MSG + "Error linking Blobs from frame " + (frame - 1) + " to frame "
							+ frame + ": " + newLinker.getErrorMessage();
					return false;
				}
				final Map<Staticproperties, Staticproperties> newAssignments = newLinker.getResult();
				final Map<Staticproperties, Double> assignmentCosts = newLinker.getAssignmentCosts();

				// Build links and new KFs from these links.
				for (final Staticproperties source : newAssignments.keySet()) {
					final Staticproperties target = newAssignments.get(source);

					// Remove from orphan collection.
					Secondorphan.remove(target);

					// Derive initial state and create Kalman filter.
					final double[] XP = estimateInitialState(source, target);
					final CVMKalmanFilter kt = new CVMKalmanFilter(XP, Double.MIN_NORMAL, positionProcessStd,
							velocityProcessStd, positionMeasurementStd);
					// We trust the initial state a lot.

					// Store filter and source
					kalmanFiltersMap.put(kt, target);

					// Add edge to the graph.
					graph.addVertex(source);
					graph.addVertex(target);
					final DefaultWeightedEdge edge = graph.addEdge(source, target);
					final double cost = assignmentCosts.get(source);
					graph.setEdgeWeight(edge, cost);

					subgraph.addVertex(source);
					subgraph.addVertex(target);
					final DefaultWeightedEdge subedge = subgraph.addEdge(source, target);
					subgraph.setEdgeWeight(subedge, cost);

					Subgraphs currentframegraph = new Subgraphs(frame - 1, frame, subgraph);

					Framedgraph.add(currentframegraph);

					final FramedBlob prevframedBlob = new FramedBlob(frame - 1, source);

					Allmeasured.add(prevframedBlob);

					final FramedBlob newframedBlob = new FramedBlob(frame, target);

					Allmeasured.add(newframedBlob);

				}
			}

			Firstorphan = Secondorphan;
			// Deal with childless KFs.
			for (final CVMKalmanFilter kf : childlessKFs) {
				// Echo we missed a measurement
				kf.update(null);

				// We can bridge a limited number of gaps. If too much, we die.
				// If not, we will use predicted state next time.
				if (kf.getNOcclusion() > maxframeGap) {
					kalmanFiltersMap.remove(kf);
				}
			}

		}

		return true;
	}

	@Override
	public void setLogger(final Logger logger) {
		this.logger = logger;

	}

	@Override
	public String getErrorMessage() {

		return errorMessage;
	}

	private static final Staticproperties MakeBlob(final ComparableRealPoint X, Staticproperties foundBlob) {
		final Staticproperties newBlob = new Staticproperties(foundBlob.currentframe, foundBlob.maxextent, X,
				foundBlob.Intensity, foundBlob.Circularity);
		return newBlob;
	}

	private static final double[] MeasureBlob(final Staticproperties target) {
		final double[] location = new double[] { target.location[0], target.location[1] };
		return location;
	}

	private static final class ComparableRealPoint extends RealPoint implements Comparable<ComparableRealPoint> {
		public ComparableRealPoint(final double[] A) {
			// Wrap array.
			super(A, false);
		}

		/**
		 * Sort based on X, Y
		 */
		@Override
		public int compareTo(final ComparableRealPoint o) {
			int i = 0;
			while (i < n) {
				if (getDoublePosition(i) != o.getDoublePosition(i)) {
					return (int) Math.signum(getDoublePosition(i) - o.getDoublePosition(i));
				}
				i++;
			}
			return hashCode() - o.hashCode();
		}
	}

	private static final double[] estimateInitialState(final Staticproperties first, final Staticproperties second) {
		final double[] xp = new double[] { second.location[0], second.location[1], second.diffTo(first, 0),
				second.diffTo(first, 1) };
		return xp;
	}

	/**
	 * 
	 * Implementations of various cost functions, starting with the simplest
	 * one, based on minimizing the distances between the links, followed by
	 * minimizing cost function based on intensity differences between the
	 * links.
	 *
	 * Cost function that returns the square distance between a KF state and a
	 * Blob.
	 */
	private static final CostFunction<ComparableRealPoint, Staticproperties> DistanceBasedcost = new CostFunction<ComparableRealPoint, Staticproperties>() {

		@Override
		public double linkingCost(final ComparableRealPoint state, final Staticproperties Blob) {
			final double dx = state.getDoublePosition(0) - Blob.location[0];
			final double dy = state.getDoublePosition(1) - Blob.location[1];
			return dx * dx + dy * dy + Double.MIN_NORMAL;
			// So that it's never 0
		}
	};

}