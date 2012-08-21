package org.openstreetmap.josm.plugins.videoplayer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Adds menu to file menu of josm.
 * Starts plugin by calling ImportVideoAction()
 * 
 * @author nikhil
 */
public class VideoPlayer extends Plugin{

	/**
	 * 
	 * @param info Provides PluginInformation
	 */
	public VideoPlayer(PluginInformation info) {
		super(info);
		MainMenu.add(Main.main.menu.fileMenu, new ImportVideoAction());

	}



}
