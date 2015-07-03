package trifonov.stanislav.segmentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class MainViewComponent extends JComponent {
	
	final static int IMG_MAX_WIDTH = 640;
	final static int IMG_MAX_HEIGHT = 480;
	final static int PALETTE_COLOR_SIZE_PX = 15;

	private BufferedImage _sourceImg;
	private BufferedImage _sourceHistogram;
	private BufferedImage _segmentedImg;
	private BufferedImage _segmentedHistogram;
	private BufferedImage _palette;
	
	public MainViewComponent(BufferedImage image) {
		_sourceImg = image;
		
		ImageProcessor ip = new ImageProcessor();
		ip.setSourceImage(_sourceImg);
		_segmentedImg = ip.process();
		_palette = ip.getPalette(PALETTE_COLOR_SIZE_PX);
		_sourceHistogram = ip.getSourceHistogramImage();
		_segmentedHistogram = ip.getSegmentedHistogram();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension( IMG_MAX_WIDTH * 2 + 2, IMG_MAX_HEIGHT + Histogram3D.CHANNEL_8_BIT + 2);
	}

	@Override
	public void paint(Graphics g) {
		int width = 0;
		int height = 0;
		float scaleFactor = 1;
		if(_sourceImg.getWidth() > IMG_MAX_WIDTH) {
			float scale = IMG_MAX_WIDTH / (float)_sourceImg.getWidth();
			if(scale < scaleFactor)
				scaleFactor = scale;
		}
		if(_sourceImg.getHeight() > IMG_MAX_HEIGHT) {
			float scale = IMG_MAX_HEIGHT / (float)_sourceImg.getHeight();
			if(scale < scaleFactor)
				scaleFactor = scale;
		}
		
		height = (int) (_sourceImg.getHeight() * scaleFactor);
		width = (int) (_sourceImg.getWidth() * scaleFactor);
		
		
		g.drawImage(_sourceImg, 0, 0, width, height, null);
		g.drawImage(_sourceHistogram, (int)(0.5*width - 0.5*_sourceHistogram.getWidth()), height+2, null);
		
		
		if(_segmentedImg != null) {
			g.drawImage(_segmentedImg, width+1, 0, width, height, null);
			g.drawImage(_palette, (int)(1.5f*width - 0.5f*_palette.getWidth()), height+2, null);
			g.drawImage(_segmentedHistogram, (int)(1.5*width-0.5*_segmentedHistogram.getWidth()), height + 4 + _palette.getHeight(), null);
		}
		 
	}
}
