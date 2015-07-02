package trifonov.stanislav.segmentation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;

public class MainViewComponent extends JComponent {

	private BufferedImage _sourceImg;
	
	public MainViewComponent(BufferedImage image) {
		_sourceImg = image;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension( _sourceImg.getWidth(), _sourceImg.getHeight() );
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(_sourceImg, 0, 0, new ImageObserver() {
			
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				ImageProcessor ip = new ImageProcessor();
				ip.process(_sourceImg);
				return false;
			}
		});
	}
}
