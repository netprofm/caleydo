package org.caleydo.view.heatmap;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.view.heatmap.creator.ViewCreatorDendrogramHorizontal;
import org.caleydo.view.heatmap.creator.ViewCreatorDendrogramVertical;
import org.caleydo.view.heatmap.creator.ViewCreatorHeatMap;
import org.caleydo.view.heatmap.creator.ViewCreatorHierarchicalHeatMap;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.caleydo.view.heatmap";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		GeneralManager.get().getViewGLCanvasManager().addViewCreator(
				new ViewCreatorHeatMap(GLHeatMap.VIEW_ID));

		GeneralManager.get().getViewGLCanvasManager().addViewCreator(
				new ViewCreatorHierarchicalHeatMap(GLHierarchicalHeatMap.VIEW_ID));
		
		GeneralManager.get().getViewGLCanvasManager().addViewCreator(
				new ViewCreatorDendrogramHorizontal(GLDendrogram.VIEW_ID+".horizontal"));
		
		GeneralManager.get().getViewGLCanvasManager().addViewCreator(
				new ViewCreatorDendrogramVertical(GLDendrogram.VIEW_ID+".vertical"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
