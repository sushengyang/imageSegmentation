package trifonov.stanislav.segmentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;

public class MainViewComponent extends JComponent {

	private BufferedImage _sourceImg;
	private BufferedImage _segmentedImg;
	
	public MainViewComponent(BufferedImage image) {
		_sourceImg = image;
		
		ImageProcessor ip = new ImageProcessor();
		_segmentedImg = ip.process(_sourceImg);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension( _sourceImg.getWidth() * 2 + 2, _sourceImg.getHeight() );
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(_sourceImg, 0, 0, null);
		if(_segmentedImg != null)
			g.drawImage(_segmentedImg, _sourceImg.getWidth()+2, 0, null);
	}
}
