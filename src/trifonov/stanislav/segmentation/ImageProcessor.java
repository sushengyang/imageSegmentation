package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import trifonov.stanislav.ml.DataPoint;
import trifonov.stanislav.ml.KMeansClustering;
import trifonov.stanislav.ml.KMeansClustering.Cluster;

public class ImageProcessor {

	private BufferedImage _sourceImage;
	private Histogram3D _histogram;
	private BufferedImage _segmentedImage;
	private Histogram3D _segmentedHistogram;
	
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
		
		_segmentedHistogram = new Histogram3D(_segmentedImage);
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
	
	public Histogram3D getHistogram() {
		return _histogram;
	}
	
	public Histogram3D getSegmentedHistogram() {
		return _segmentedHistogram;
	}
	
	public BufferedImage getSourceHistogramImage() {
		return histogramImage( _histogram, _sourceImage.getType() );
	}
	
	public BufferedImage getSegmentedHistogramImage() {
		return histogramImage( _segmentedHistogram, _segmentedImage.getType() );
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
	
	public static void exportHistogramToObj(Histogram3D histogram, File objFile) throws IOException {
		if(objFile.exists())
			objFile.delete();

		String name = objFile.getName();
		if(name.contains("."))
			name = name.substring(0, name.indexOf('.'));
		
		File mtlFile = new File(objFile.getParent(), name +".mtl");
		List<int[]> colors = histogram.getColorPoints();
		exportMaterialsFile(colors, mtlFile);
		
		Set<OpenOption> options = new HashSet<OpenOption>();
	    options.add(StandardOpenOption.APPEND);
	    options.add(StandardOpenOption.CREATE);
	    Path path = Paths.get( objFile.getAbsolutePath() );
	    SeekableByteChannel sbc = Files.newByteChannel(path, options);

	    String useMtlHeader = "mtllib " + mtlFile.getName() + '\n';
	    sbc.write( ByteBuffer.wrap(useMtlHeader.getBytes()) );
	    
	    StringBuilder lineBuilder = new StringBuilder();
	    for(int[] color : colors) {
	    	lineBuilder.setLength(0);
	    	float red = color[0] / (float)Histogram3D.CHANNEL_8_BIT;
	    	float green = color[1] / (float)Histogram3D.CHANNEL_8_BIT;
	    	float blue = color[2] / (float)Histogram3D.CHANNEL_8_BIT;
	    	lineBuilder.append(
	    			"v " + red
	    			+ " " + green
	    			+ " " + blue
	    			+ '\n');
	    	
	    	int rgb = (color[0] << 16) | (color[1] << 8) | color[2];
	    	String useMtlLine = "usemtl mtl" + rgb + '\n';
	    	
	    	sbc.write( ByteBuffer.wrap(useMtlLine.getBytes()) );
	    	sbc.write( ByteBuffer.wrap(lineBuilder.toString().getBytes()) );
	    }
	}
	
	private static void exportMaterialsFile(List<int[]> colors, File mtlFile) throws IOException {
		if(mtlFile.exists())
			mtlFile.delete();
		
		Set<OpenOption> options = new HashSet<OpenOption>();
	    options.add(StandardOpenOption.APPEND);
	    options.add(StandardOpenOption.CREATE);
	    Path path = Paths.get( mtlFile.getAbsolutePath() );
	    SeekableByteChannel sbc = Files.newByteChannel(path, options);
	    
	    StringBuilder lineBuilder = new StringBuilder();
	    
	    for(int[] color : colors) {
	    	lineBuilder.setLength(0);
	    	float red = color[0] / (float)Histogram3D.CHANNEL_8_BIT;
	    	float green = color[1] / (float)Histogram3D.CHANNEL_8_BIT;
	    	float blue = color[2] / (float)Histogram3D.CHANNEL_8_BIT;
	    	
	    	int rgb = (color[0] << 16) | (color[1] << 8) | color[2];
	    	String mtlName = "newmtl mtl" +  rgb + '\n';
	    	String diffuseColorRow = 
	    			"Kd " + red
	    			+ " " + green
	    			+ " " + blue
	    			+ '\n';

	    	sbc.write( ByteBuffer.wrap( mtlName.getBytes() ) );
	    	sbc.write( ByteBuffer.wrap( diffuseColorRow.getBytes() ) );
	    }
	}
}
