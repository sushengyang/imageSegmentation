package trifonov.stanislav.segmentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;

public class MainViewComponent extends JComponent {
	
	final static int IMG_MAX_WIDTH = 640;
	final static int IMG_MAX_HEIGHT = 480;

	private BufferedImage _sourceImg;
	private BufferedImage _segmentedImg;
	
	public MainViewComponent(BufferedImage image) {
		_sourceImg = image;
		
		ImageProcessor ip = new ImageProcessor();
		ip.setSourceImage(_sourceImg);
		_segmentedImg = ip.process();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension( IMG_MAX_WIDTH * 2 + 2, IMG_MAX_HEIGHT );
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
		
		if(_segmentedImg != null)
			g.drawImage(_segmentedImg, width+1, 0, width, height, null);
	}
}
