package trifonov.stanislav.segmentation;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;

public class Histogram3D {

	public static final int CHANNEL_8_BIT = 256;
	
	private final int _histogram[][][] = new int[CHANNEL_8_BIT][CHANNEL_8_BIT][CHANNEL_8_BIT];
	private final BufferedImage _image;
	private int _uniqueColorsCount;
	
	public Histogram3D(BufferedImage image) {
		_image = image;
		
		build();
	}
	
	private void build() {
		ColorModel cm = _image.getColorModel();
		
		for(int y=0; y<_image.getHeight(); ++y) {
			for(int x=0; x<_image.getWidth(); ++x) {
				int pixel = _image.getRGB(x, y);
				int red = (pixel & 0x00ff0000) >> 16;
				int green = (pixel & 0x0000ff00) >> 8;
				int blue = pixel & 0x000000ff;
				
				_histogram[red][green][blue] += 1;
			}
		}
		
		for(int r=0; r<_histogram.length; ++r)
			for(int g=0; g<_histogram[r].length; ++g)
				for(int b=0; b<_histogram[r][g].length; ++b)
					if( _histogram[r][g][b] > 0 )
						++_uniqueColorsCount;
	}
	
	public int getUniqueColorsCount() {
		return _uniqueColorsCount;
	}
	
	/**
	 * Returns a list of unique color points. Each color point is represented by a 3-component (r,g,b) array.
	 * Each color is returned once, no matter how many times it's found in the image
	 * @return the list of point
	 */
	public List<int[]> getColorPoints() {
		List<int[]> colors = new ArrayList<>(_uniqueColorsCount);
		
		for(int r=0; r<_histogram.length; ++r) {
			for(int g=0; g<_histogram[r].length; ++g) {
				for(int b=0; b<_histogram[r][g].length; ++b) {
					if( _histogram[r][g][b] > 0 )
						colors.add( new int[] {r, g, b});
				}
			}
		}
		
		return colors;
	}
}
