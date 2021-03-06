package org.openstreetmap.josm.plugins.videoplayer;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.GpxLayer;

/**
 * Provides dialog to import video in josm.
 * Videolayer object is created and videofile is provided to object.
 * 
 * @author nikhil
 */
public class ImportVideoAction extends JosmAction{
	GpxLayer gpxLayer ;

	public ImportVideoAction() {
		super( "Import Video", null, "Import Video", null, true);
	}
	public void actionPerformed(ActionEvent arg0) {


		final JFileChooser fileChooser = new JFileChooser();

		fileChooser.setFileFilter(new VideoFileFilter());

		if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {

		}
		final String videoPath = fileChooser.getSelectedFile()
				.getAbsolutePath();

		VideoLayer vl = new VideoLayer(videoPath);
		vl.pack();
		vl.setVisible(true);

	}

	/**
	 * 
	 * @author nikhil
	 * 
	 * Provides filter for JFileChooser
	 */
	private static final class VideoFileFilter extends FileFilter {

		@Override
		public boolean accept(File file) {
			return file.isDirectory()
					|| file.getName().toLowerCase().endsWith(".mpg")
					|| file.getName().toLowerCase().endsWith(".mpeg")
					|| file.getName().toLowerCase().endsWith(".avi")
					|| file.getName().toLowerCase().endsWith(".mp4")
					|| file.getName().toLowerCase().endsWith(".mov")
					|| file.getName().toLowerCase().endsWith(".MOV");

		}

		@Override
		public String getDescription() {
			return null;
		}

	}

}