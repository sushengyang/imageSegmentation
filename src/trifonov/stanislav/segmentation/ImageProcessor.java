package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import trifonov.stanislav.ml.DataPoint;
import trifonov.stanislav.ml.KMeansClustering;
import trifonov.stanislav.ml.KMeansClustering.Cluster;

public class ImageProcessor {

	public static class ClusterableColor implements Clusterable {
		
		double[] _rgb = new double[3];

		public ClusterableColor(int r, int g, int b) {
			_rgb[0] = r;
			_rgb[1] = g;
			_rgb[2] = b;
		}
		
		@Override
		public double[] getPoint() {
			return _rgb;
		}
		
	}
	
	
	public ImageProcessor() {

	}

	public BufferedImage process(BufferedImage image) {
		
		Histogram3D histogram = new Histogram3D(image);
		System.out.println("colors found: " + histogram.getUniqueColorsCount());
		List<Cluster> clusters = cluster(histogram);
		
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for(int y=0; y<result.getHeight(); ++y) {
			for(int x=0; x<result.getWidth(); ++x) {
				int pixel = image.getRGB(x, y);
				int red = (pixel & 0x00ff0000) >> 16;
				int green = (pixel & 0x0000ff00) >> 8;
				int blue = pixel & 0x000000ff;
				DataPoint color = new DataPoint(red, green, blue);
				DataPoint centroidColor = KMeansClustering.clusterAffiliation(color, clusters);
				int newColor = (centroidColor._r << 16) | (centroidColor._g << 8) | centroidColor._b;
				result.setRGB(x, y, newColor);
			}
		}
		
		return result;
	}
	
	private List<Cluster> cluster(Histogram3D histogram) {
		long start = System.currentTimeMillis();
		
		int k = 8;//log2(histogram.getUniqueColorsCount());
		KMeansClustering kmeans = new KMeansClustering(k);
		List<int[]> colors = histogram.getColorPoints();
		List<DataPoint> dataPoints = new ArrayList<>(colors.size());
		for(int[] color : colors)
			dataPoints.add( new DataPoint(color[0], color[1], color[2]) );
		kmeans.setData(dataPoints);
		List<Cluster> clusters = kmeans.cluster();
		
//		KMeansPlusPlusClusterer<ClusterableColor> clusterer = new KMeansPlusPlusClusterer<>(k);
//		List<ClusterableColor> colorData = new ArrayList<>();
//		for(int[] color : colors)
//			colorData.add( new ClusterableColor(color[0], color[1], color[2]) );
//		List<CentroidCluster<ClusterableColor>> clusters = clusterer.cluster(colorData);
		
		long end = System.currentTimeMillis();
		System.out.println("Clustering took " + (end-start) + "ms.");
		
		return clusters;
	}
	
	public void getHistogramImage() {
		
	}
	
	public static int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}
}
