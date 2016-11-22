package probabilityMatrix;

import java.awt.Color;
import java.util.ArrayList;

import graphconstructs.Logger;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mserMethods.GetDelta;
import mserTree.GetMSERtree;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import segmentBlobs.GlobalThresholding;
import segmentBlobs.Staticproperties;

public class BlobfinderMSER implements Blobfinder {

	
	private static final String BASE_ERROR_MSG = "[Blobfinder] ";
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;
	private ArrayList<ArrayList<Staticproperties>> ProbBlobs;
	private final RandomAccessibleInterval<FloatType> source;
	private final RandomAccessibleInterval<FloatType> Preprocessedsource;
	private final double[] calibration;
	public double delta = 20;
	public long minSize = 1;
	public long maxSize = Long.MAX_VALUE;
	public  double maxVar = 0.5;
	public  double minDiversity = 0;
	public int maxblobs = 40;
	public int maxdelta = 20;
	public boolean darktoBright = false;
	public int mindiameter = 1;
	private final int ndims;
	public void setDelta(double delta) {
		this.delta = delta;
	}
	
	public void setDarktoBright(boolean darktoBright) {
		this.darktoBright = darktoBright;
	}
	

	
	public void setMaxblobs(int maxblobs) {
		this.maxblobs = maxblobs;
	}
	
	public void setMinDiversity(double minDiversity) {
		this.minDiversity = minDiversity;
	}
	
	public void setMaxVar(double maxVar) {
		this.maxVar = maxVar;
	}
	 public long getMinSize() {
		return minSize;
	}
	 
	
	public long getMaxSize() {
		return maxSize;
	} 
	 
	 public int getMaxdelta() {
		return maxdelta;
	}
	 
	 public double getMaxVar() {
		return maxVar;
	}
	 
	 public int getMindiamter() {
		return mindiameter;
	}
	 
	 public double getMinDiversity() {
		return minDiversity;
	}
	 
	
	
	
	public BlobfinderMSER(final RandomAccessibleInterval<FloatType> source, 
			final RandomAccessibleInterval<FloatType> Preprocessedsource,
			final double[] calibration){
		
		this.source = source;
		this.Preprocessedsource = Preprocessedsource;
		this.calibration = calibration;
		ndims = source.numDimensions();
	}
	
	
	@Override
	public ArrayList<ArrayList<Staticproperties>> getResult() {

		
		return ProbBlobs;
	}

	@Override
	public boolean checkInput() {
		if (source.numDimensions() > 2 || Preprocessedsource.numDimensions() > 2) {
			errorMessage = BASE_ERROR_MSG + " Can only operate on 1D, 2D, make slices of your stack . Got "
					+ source.numDimensions() + "D.";
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		
		// Do the MSER detection to find blobs
		
		final Img<UnsignedByteType> newimg;

		try
		{
		ImageJFunctions.wrap(Preprocessedsource, "curr");
		final ImagePlus currentimp = IJ.getImage();
		IJ.run("8-bit");

		newimg = ImagePlusAdapter.wrapByte(currentimp);

		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			return false;
		}

		System.out.println("Choosing best delta:");
		double bestdelta = GetDelta.Bestdeltaparam(newimg, delta, minSize, maxSize, maxVar, minDiversity,
				mindiameter, maxblobs, maxdelta, darktoBright);
		MserTree<UnsignedByteType> newtreeBtoD = MserTree.buildMserTree(newimg,
				bestdelta, minSize, maxSize, maxVar, minDiversity, darktoBright);

		final ImagePlus currentimp = IJ.getImage();
		final GetMSERtree<UnsignedByteType> visualizetree = new GetMSERtree<UnsignedByteType>(currentimp);
		final ArrayList<double[]> ellipselist = visualizetree.Roiarraylist(newtreeBtoD);
		visualizetree.visualise(ellipselist, Color.green);
		
		
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
		
	}

	private double[] ComputeProbability(final RandomAccessibleInterval<FloatType> currentimg){
		
		
		FinalInterval smallinterval = new FinalInterval(currentimg);
		 double size = 0;
		
		for (int d = 0; d < ndims; ++d)
			size*=currentimg.dimension(d);

		double sigma1 =  size / 2 ;
		double sigma2 = 1.2 * sigma1 ;
		Float val = GlobalThresholding.AutomaticThresholding(currentimg);
		Float threshold = new Float(val);

		DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentimg), smallinterval,
				new double[] { calibration[0], calibration[1] }, sigma1, sigma2, DogDetection.ExtremaType.MINIMA,
				threshold, true);
		ArrayList<RefinedPeak<Point>> SubpixelMinlist = new ArrayList<RefinedPeak<Point>>(ndims);
		double MaxIntensity = Double.MIN_VALUE;
		SubpixelMinlist = newdog.getSubpixelPeaks();

		final double[] probability = new double[SubpixelMinlist.size()];
		for (int index = 0; index < SubpixelMinlist.size(); ++index) {
            double intensity = SubpixelMinlist.get(index).getValue();
			
			if (intensity > MaxIntensity){
				
				MaxIntensity = intensity;
			}
			
		}
		
		for (int index = 0; index < SubpixelMinlist.size(); ++index) {
			
			double[] peaks = {SubpixelMinlist.get(index).getDoublePosition(0), SubpixelMinlist.get(index).getDoublePosition(1)};
			double intensity = SubpixelMinlist.get(index).getValue();
			
			
			if (SubpixelMinlist.size() > 1){
				double sqdistance = 0;
			for (int secondindex = 0; secondindex < SubpixelMinlist.size(); ++secondindex) {
				if (secondindex != index){
				double[] otherpeaks = {SubpixelMinlist.get(secondindex).getDoublePosition(0), SubpixelMinlist.get(secondindex).getDoublePosition(1)};
				
				sqdistance += sqDistance(peaks, otherpeaks);
				}
		 }
			
			
			probability[index] = intensity / MaxIntensity * (sqdistance/ size);
			
			}
			
			else
				probability[index] = intensity / MaxIntensity ;
			
			
		}
			
		return probability;
	}
	public double sqDistance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return (distance);
	}
}
