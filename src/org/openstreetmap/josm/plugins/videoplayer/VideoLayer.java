package org.openstreetmap.josm.plugins.videoplayer;

import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_highgui.cvNamedWindow;
import static com.googlecode.javacv.cpp.opencv_highgui.cvShowImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvWaitKey;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CascadeClassifier;

/**
 * This class provides GUI for plugin, creates markerlayer on map and
 * interacts with video frames and gpx layer.
 * @author nikhil
 *
 */
public class VideoLayer extends JFrame{

	private final JFileChooser fileChooser = new JFileChooser();
	/**
	 * Stores Location of video file
	 */
	public String videoPath;
	/**
	 * IplImage for temp storage
	 */
	private IplImage image = null;

	/**
	 * Total number of frames in video
	 */
	public int frameCount;

	OpenCVFrameGrabber grabber;
	GpxLayer gpxLayer;
	MarkerLayer markerLayer;
	/**
	 * Current position Marker
	 */
	Marker 	currentPos;
	WayPoint wp, wp_temp;
	/**
	 * collection of waypoints from gpxdata
	 */
	Collection<WayPoint> wpCol;
	int wp_num;
	double posIndex;
	int num, pauseNum;
	boolean pause = false;
	/**
	 * Stores locations of training file
	 */
	public String CASCADE_FILE = null;

	/**
	 * Creates GUI which displays video and interacts with to change the
	 * location of maker according to currnt frame.
	 * 
	 * @param videoPath Location of video file
	 * @throws HeadlessException
	 */

	public VideoLayer(final String videoPath) throws HeadlessException{
		super("VideoPlayer");
		CreateMarkerLayer();
		this.videoPath = videoPath;
		startGrabber();

		/**
		 * create canvas to display images
		 */
		final CanvasFrame canvas = new CanvasFrame("Video");
		canvas.setCanvasSize(640, 480);
		Main.map.mapView.zoomToFactor(0.15);
		//cvNamedWindow("video");
		//cvResizeWindow("video", 640, 480);
		getFrame();


		final Action playAction = new AbstractAction("Play") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if(pause){
						pause = false;
					}
					for(num = grabber.getFrameNumber();
							num < grabber.getLengthInFrames(); num ++){
						try {
							KeyEvent key = canvas.waitKey(10);
							if(key != null){
								break;
							}
						} catch (InterruptedException e1) {

						}
						image = grabber.grab();
						if (image != null) {
							canvas.showImage(image);
							//cvShowImage("video", image);
							//cvResizeWindow("video", 640, 480);
							//cvWaitKey(0);
							posIndex = wp_num*((double)grabber.getFrameNumber()/(double)frameCount);
							int temp = 0;
							for(WayPoint p1: wpCol){
								wp_temp = p1;
								if(temp > posIndex){
									break;
								}
								temp ++;
							}
							markerLayer.data.clear();
							currentPos = new Marker(wp_temp.getCoor(),
									"Current Position",	null, null, -1.0, 0.0);
							markerLayer.data.add(currentPos);
							Main.map.mapView.zoomTo(wp_temp.getCoor());


							if(pause){
								break;
							}
						}
					}

				} catch (Exception e1) {
				}
				finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		final Action pauseAction = new AbstractAction("Pause") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					pause = true;
					pauseNum = grabber.getFrameNumber();
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		final Action setCascadeFileAction = new AbstractAction("Set Cascade file") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					CASCADE_FILE = getCascadeFile();

				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		final Action nextAction = new AbstractAction(">") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					getFrame();
					if (image != null) {
						canvas.showImage(image);
						//cvShowImage("video", image);cvResizeWindow("video", 640, 480);
						//cvWaitKey(0);

						posIndex = wp_num*((double)grabber.getFrameNumber()/(double)frameCount);
						int temp = 0;
						for(WayPoint p1: wpCol){
							wp_temp = p1;
							if(temp > posIndex){
								break;
							}
							temp ++;
						}

						markerLayer.data.clear();
						currentPos = new Marker(wp_temp.getCoor(),
								"Current Position",	null, null, -1.0, 0.0);
						markerLayer.data.add(currentPos);
						Main.map.mapView.zoomTo(wp_temp.getCoor());
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		final Action processAction = new AbstractAction("Process") {
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if(CASCADE_FILE == null){
						JOptionPane.showMessageDialog(
								Main.parent,
								"Set Cascade file first",
								"Error",
								JOptionPane.ERROR_MESSAGE
								);
					}
					else{
						cvNamedWindow("Detected Images : press any key to show next image");

						try {
							grabber.restart();
							while(true){
								IplImage processImage = grabber.grab();
								if(processImage != null){
									if(ProcessImage(processImage)){
										cvShowImage("Detected Images",processImage);
										cvWaitKey(0);
									}
								}
							}

						} catch (Exception e1) {

						}}

				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		final JPanel buttonsPanel = new JPanel(new GridLayout(0, 5, 4, 8));
		buttonsPanel.add(new JButton(playAction));
		buttonsPanel.add(new JButton(nextAction));
		buttonsPanel.add(new JButton(pauseAction));
		buttonsPanel.add(new JButton(processAction));
		buttonsPanel.add(new JButton(setCascadeFileAction));
		final JPanel botPanel = new JPanel();
		botPanel.add(buttonsPanel);
		add(botPanel, BorderLayout.SOUTH);

	}

	/**
	 * startGrabber() starts OpenCVFrameGrabber which starts grabbing videofiles
	 * from video
	 */
	public void startGrabber(){

		grabber = new OpenCVFrameGrabber(videoPath);
		try {
			grabber.start();
			frameCount = grabber.getLengthInFrames();
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			JOptionPane.showMessageDialog(
					Main.parent,
					"Cannot play this video file",
					"Error",
					JOptionPane.ERROR_MESSAGE
					);
		}
	}

	/**
	 * grabs frame from grabber and saves it to IplImage image.
	 */
	public void getFrame() {
		IplImage img = null;
		try {
			img = grabber.grab();

		} catch (Exception e) {

		}
		image = img;
	}

	/**
	 * Creats markerlayer on map with gpxdata provided by existing gpxlayer
	 */
	public void CreateMarkerLayer(){

		Collection<Layer> layers = Main.map.mapView.getAllLayers();

		for (Layer layer : layers){
			if(layer instanceof GpxLayer) {
				gpxLayer = (GpxLayer) layer;
				break;
			}
		}
		markerLayer = new MarkerLayer(gpxLayer.data,"VideoPlayer", null, null);
		Main.main.addLayer(markerLayer);
		markerLayer.setVisible(true);
		Collection<GpxTrack> w = gpxLayer.data.tracks;
		if(w.size() == 0){

		}
		else{
			wpCol = w.iterator().next()
					.getSegments().iterator().next()
					.getWayPoints();

			wp_num = 0;
			for (WayPoint temp : wpCol){
				if(wp_num == 1){
					wp = temp;
				}
				wp_num ++;
			}
		}
	}

	/**
	 * process input image frame for given sign detection
	 * @param src input image frame
	 * @return true if sign detected
	 */
	public boolean ProcessImage(IplImage src){

		IplImage gray = cvCreateImage(cvGetSize(src), 8, 1);

		CvRect rects = new CvRect();
		rects.setNull();

		CascadeClassifier cascade = new CascadeClassifier();
		cascade.load(CASCADE_FILE);

		CvMemStorage storage = CvMemStorage.create();

		cvCvtColor(src, gray, CV_BGR2GRAY );

		cascade.detectMultiScale(src,
				rects,
				1.1,  // scale
				1,   // min neighbours
				0,
				cvSize(10, 10),
				cvSize(100, 100));

		cvClearMemStorage(storage);

		if(rects.isNull()){
			return false;
		}
		else{
			return true;
		}

	}

	/**
	 * invoke JFileChooser to select cascade file
	 */
	public String getCascadeFile(){
		if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		final String cascadePath = fileChooser.getSelectedFile().getAbsolutePath();
		return cascadePath;
	}
}