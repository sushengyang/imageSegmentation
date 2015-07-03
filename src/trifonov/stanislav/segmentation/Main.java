package trifonov.stanislav.segmentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Main {
	
	static final String PATH_TEST_IMAGES = "/home/stan0/Desktop/standard-test-images/kodak_set_bmp";
	static final String IMAGE_NAME_LENNA = "IMG0023.bmp";
	

	public static void main(String[] args) throws IOException {
		
		final JFrame frame = new JFrame();
		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				super.windowClosing(arg0);
				System.exit(0);
			}
		});
		
		final JFileChooser fc = new JFileChooser();
		final MainViewComponent mainComponent = new MainViewComponent();
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem itemOpen = new JMenuItem("Open");
		itemOpen.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showOpenDialog(mainComponent);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            try {
						mainComponent.openImage( fc.getSelectedFile() );
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		        }
			}
		});
		menu.add(itemOpen);
		menuBar.add(menu);
		
		frame.setJMenuBar(menuBar);
		frame.add( mainComponent );
		frame.pack();
		frame.setVisible(true);
	}
}
