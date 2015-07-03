package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import trifonov.stanislav.ml.DataPoint;
import trifonov.stanislav.ml.KMeansClustering;
import trifonov.stanislav.ml.KMeansClustering.Cluster;

public class ImageProcessor {

	private BufferedImage _sourceImage;
	private Histogram3D _histogram;
	private BufferedImage _segmentedImage;
	
	public ImageProcessor() {

	}

	public void setSourceImage(BufferedImage image) {
		_sourceImage = image;
	}

	public BufferedImage process() {
		
		_histogram = new Histogram3D(_sourceImage);
		System.out.println("colors found: " + _histogram.getUniqueColorsCount());

		int maxK = log2(_histogram.getUniqueColorsCount());
		for(int k=maxK; k<=maxK; ++k) {
			long start = System.currentTimeMillis();
			KMeansClustering kmeans = new KMeansClustering(k);
			List<int[]> colors = _histogram.getColorPoints();
			List<DataPoint> dataPoints = new ArrayList<>(colors.size());
			for(int[] color : colors)
				dataPoints.add( new DataPoint(color[0], color[1], color[2]) );
			kmeans.setData(dataPoints);
			List<Cluster> clusters = kmeans.cluster();
			
			long end = System.currentTimeMillis();
//			System.out.println("Clustering took " + (end-start) + "ms.");
		
			_segmentedImage = new BufferedImage(_sourceImage.getWidth(), _sourceImage.getHeight(), _sourceImage.getType());
			for(int y=0; y<_segmentedImage.getHeight(); ++y) {
				for(int x=0; x<_segmentedImage.getWidth(); ++x) {
					int pixel = _sourceImage.getRGB(x, y);
					int alpha = (pixel & 0xff) >> 24;
					int red = (pixel & 0x00ff0000) >> 16;
					int green = (pixel & 0x0000ff00) >> 8;
					int blue = pixel & 0x000000ff;
					DataPoint color = new DataPoint(red, green, blue);
					DataPoint centroidColor = KMeansClustering.clusterAffiliation(color, clusters);
					int newColor = (alpha << 24) | (centroidColor._r << 16) | (centroidColor._g << 8) | centroidColor._b;
					_segmentedImage.setRGB(x, y, newColor);
				}
			}
			
			System.out.println("k=" +k+ " -> " + informationLoss() + "% ("+(end-start)+"ms)");
		}
		
		return _segmentedImage;
	}
	
	public void getHistogramImage() {
		
	}
	
	public static int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}
	
	public double informationLoss() {
		int changedPixelsCount = 0;
		
		for(int y=0; y<_sourceImage.getHeight(); ++y) {
			for(int x=0; x<_sourceImage.getWidth(); ++x) {
				int pixel_original = _sourceImage.getRGB(x, y);
				int red_original = (pixel_original & 0x00ff0000) >> 16;
				int green_original = (pixel_original & 0x0000ff00) >> 8;
				int blue_original = pixel_original & 0x000000ff;
				
				int pixel_segmented = _segmentedImage.getRGB(x, y);
				int red_segmented = (pixel_segmented & 0x00ff0000) >> 16;
				int green_segmented = (pixel_segmented & 0x0000ff00) >> 8;
				int blue_segmented = pixel_segmented & 0x000000ff;
				
				if( red_segmented - red_original != 0
						|| green_segmented - green_original != 0
						|| blue_segmented - blue_original != 0 )
					++changedPixelsCount;
			}
		}
		
		return (int) (100*changedPixelsCount / (double)((_sourceImage.getHeight() * _sourceImage.getWidth())));
	}
	
	public BufferedImage getPalette(int colorSize) {
		Histogram3D histogram = new Histogram3D(_segmentedImage);
		histogram.getUniqueColorsCount();
		List<int[]> colors = histogram.getColorPoints();
		
		BufferedImage result = new BufferedImage(colorSize*colors.size(), colorSize, _segmentedImage.getType());
		for(int i=0; i<colors.size(); ++i) {
			for(int y=0; y<colorSize; ++y) {
				for(int x=i*colorSize; x<(i+1)*colorSize; ++x) {
					int[] color = colors.get(i);
					int newColor = (color[0] << 16) | (color[1] << 8) | color[2];
					result.setRGB(x, y, newColor);
				}
			}
		}
		
		return result;
	}
	
	public BufferedImage getSourceHistogramImage() {
		return histogramImage( _histogram, _sourceImage.getType() );
	}
	
	public BufferedImage getSegmentedHistogram() {
		return histogramImage( new Histogram3D(_segmentedImage), _segmentedImage.getType() );
	}
	
	private static BufferedImage histogramImage(Histogram3D histogram, int imageType) {
		List<int[]> colors = histogram.getColorPoints();
		
		BufferedImage histogramImg = new BufferedImage(Histogram3D.CHANNEL_8_BIT+1, Histogram3D.CHANNEL_8_BIT+1, imageType);
		
		for(int[] color : colors) {
			int rgb = (color[0] << 16) | (color[1] << 8) | color[2];
			histogramImg.setRGB(color[0], color[1], rgb);
		}
		for(int y=0; y<Histogram3D.CHANNEL_8_BIT+1; ++y) {
			int rgb = 255 << 16;
			histogramImg.setRGB(y, 1, rgb);
		}
		for(int x=0; x<Histogram3D.CHANNEL_8_BIT+1; ++x) {
			int rgb = 255 << 8;
			histogramImg.setRGB(1, x, rgb);
		}
		
		return histogramImg;
	}
}
