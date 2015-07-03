package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import trifonov.stanislav.ml.DataPoint;
import trifonov.stanislav.ml.KMeansClustering;
import trifonov.stanislav.ml.KMeansClustering.Cluster;

public class ImageProcessor {

	private BufferedImage _sourceImage;
	
	public ImageProcessor() {

	}

	public void setSourceImage(BufferedImage image) {
		_sourceImage = image;
	}

	public BufferedImage process() {
		
		Histogram3D histogram = new Histogram3D(_sourceImage);
		System.out.println("colors found: " + histogram.getUniqueColorsCount());
		BufferedImage result = null;

		int maxK = log2(histogram.getUniqueColorsCount());
		for(int k=maxK; k<=maxK; ++k) {
			long start = System.currentTimeMillis();
			KMeansClustering kmeans = new KMeansClustering(k);
			List<int[]> colors = histogram.getColorPoints();
			List<DataPoint> dataPoints = new ArrayList<>(colors.size());
			for(int[] color : colors)
				dataPoints.add( new DataPoint(color[0], color[1], color[2]) );
			kmeans.setData(dataPoints);
			List<Cluster> clusters = kmeans.cluster();
			
			long end = System.currentTimeMillis();
//			System.out.println("Clustering took " + (end-start) + "ms.");
		
			result = new BufferedImage(_sourceImage.getWidth(), _sourceImage.getHeight(), _sourceImage.getType());
			for(int y=0; y<result.getHeight(); ++y) {
				for(int x=0; x<result.getWidth(); ++x) {
					int pixel = _sourceImage.getRGB(x, y);
					int alpha = (pixel & 0xff) >> 24;
					int red = (pixel & 0x00ff0000) >> 16;
					int green = (pixel & 0x0000ff00) >> 8;
					int blue = pixel & 0x000000ff;
					DataPoint color = new DataPoint(red, green, blue);
					DataPoint centroidColor = KMeansClustering.clusterAffiliation(color, clusters);
					int newColor = (alpha << 24) | (centroidColor._r << 16) | (centroidColor._g << 8) | centroidColor._b;
					result.setRGB(x, y, newColor);
				}
			}
			
			System.out.println("k=" +k+ " -> " + informationLoss(_sourceImage, result) + "% ("+(end-start)+"ms)");
		}
		
		return result;
	}
	
	public void getHistogramImage() {
		
	}
	
	public static int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}
	
	public static double informationLoss(BufferedImage original, BufferedImage segmented) {
		int changedPixelsCount = 0;
		
		for(int y=0; y<original.getHeight(); ++y) {
			for(int x=0; x<original.getWidth(); ++x) {
				int pixel_original = original.getRGB(x, y);
				int red_original = (pixel_original & 0x00ff0000) >> 16;
				int green_original = (pixel_original & 0x0000ff00) >> 8;
				int blue_original = pixel_original & 0x000000ff;
				
				int pixel_segmented = segmented.getRGB(x, y);
				int red_segmented = (pixel_segmented & 0x00ff0000) >> 16;
				int green_segmented = (pixel_segmented & 0x0000ff00) >> 8;
				int blue_segmented = pixel_segmented & 0x000000ff;
				
				if( red_segmented - red_original != 0
						|| green_segmented - green_original != 0
						|| blue_segmented - blue_original != 0 )
					++changedPixelsCount;
			}
		}
		
		return (int) (100*changedPixelsCount / (double)((original.getHeight() * original.getWidth())));
	}
}
