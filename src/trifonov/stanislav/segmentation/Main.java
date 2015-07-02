package trifonov.stanislav.segmentation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Main {
	
	static final String PATH_TEST_IMAGES = "/home/stan0/Desktop/standard-test-images";
	static final String IMAGE_NAME_LENNA = "Lenna.png";
	

	public static void main(String[] args) throws IOException {
		BufferedImage img = ImageIO.read( new File(PATH_TEST_IMAGES, IMAGE_NAME_LENNA) );
		
		
		JFrame frame = new JFrame();
		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				super.windowClosing(arg0);
				System.exit(0);
			}
		});
		
		frame.add( new MainViewComponent(img) );
		frame.pack();
		frame.setVisible(true);
		
		ImageProcessor processor = new ImageProcessor();
		processor.process(img);
	}
}
