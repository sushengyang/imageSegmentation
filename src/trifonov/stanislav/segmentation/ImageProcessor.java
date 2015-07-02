package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import trifonov.stanislav.ml.DataPoint;
import trifonov.stanislav.ml.KMeansClustering;

public class ImageProcessor {

	public ImageProcessor() {

	}

	public void process(BufferedImage image) {
		
		Histogram3D histogram = new Histogram3D(image);
		cluster(histogram);
		
		
		System.out.println("colors found: " + histogram.getUniqueColorsCount());
		//return segmented image
	}
	
	void cluster(Histogram3D histogram) {
		List<int[]> colors = histogram.getColorPoints();
		List<DataPoint> dataPoints = new ArrayList<>(colors.size());
		for(int[] color : colors)
			dataPoints.add( new DataPoint(color[0], color[1], color[2]) );
		
		
		long start = System.currentTimeMillis();
		
		KMeansClustering kmeans = new KMeansClustering( log2(histogram.getUniqueColorsCount()) );
		kmeans.setData(dataPoints);
		kmeans.cluster();
		
		long end = System.currentTimeMillis();
		System.out.println("Clustering took " + (end-start) + "ms.");
	}
	
	public void getHistogramImage() {
		
	}
	
	public static int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}
}
