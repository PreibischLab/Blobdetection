package segmentBlobs;

import java.util.ArrayList;

import net.imglib2.Point;
import net.imglib2.algorithm.localextrema.RefinedPeak;

public final class Labelmax {

	
	public final int Label;
	
	public final ArrayList<RefinedPeak<Point>> Totalminlist;
	
	
	protected Labelmax(final int Label, final ArrayList<RefinedPeak<Point>> Totalminlist ){
		
		this.Label = Label;
		this.Totalminlist = Totalminlist;
	}
	
}
