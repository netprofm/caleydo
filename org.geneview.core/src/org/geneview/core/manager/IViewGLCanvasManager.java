/**
 * 
 */
package org.geneview.core.manager;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;

import org.geneview.core.command.CommandQueueSaxType;
import org.geneview.core.manager.view.PickingManager;
import org.geneview.core.manager.view.SelectionManager;
import org.geneview.core.view.jogl.JoglCanvasForwarderType;
import org.geneview.core.view.opengl.IGLCanvasDirector;
import org.geneview.core.view.opengl.IGLCanvasUser;
import org.geneview.core.view.swt.data.search.DataEntitySearcherViewRep;

/**
 * Make Jogl GLCanvas addressable by id and provide ground for XML bootstrapping of 
 * GLCanvas & GLEventListener.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 *
 */
public interface IViewGLCanvasManager 
extends IViewManager {
	
	public IGLCanvasUser createGLCanvasUser(CommandQueueSaxType useViewType,
			final int iUniqueId, 
			final int iGlForwarderId,
			String sLabel );

	public boolean registerGLCanvas( final GLCanvas canvas, final int iCanvasId );
	
	public boolean unregisterGLCanvas( final GLCanvas canvas );
	
	public boolean registerGLCanvasUser( final IGLCanvasUser canvas, 
			final int iCanvasId );
	
	public boolean unregisterGLCanvasUser( final IGLCanvasUser canvas);
	
	public GLEventListener getGLEventListener( int iId );
	
	public boolean registerGLEventListener( final GLEventListener canvasListener, 
			final int iId );
	
	public boolean unregisterGLEventListener( final GLEventListener canvasListener );
	
	public boolean addGLEventListener2GLCanvasById( final int iCanvasListenerId, 
			final int iCanvasId );
	
	public boolean removeGLEventListener2GLCanvasById(final int iCanvasListenerId, 
			final int iCanvasId);
	
	public IGLCanvasDirector getGLCanvasDirector( int iId );
	
	public boolean registerGLCanvasDirector( final IGLCanvasDirector director, final int iId );
	
	public boolean unregisterGLCanvasDirector( final IGLCanvasDirector director );
	
	public void setJoglCanvasForwarderType(JoglCanvasForwarderType type);
	
	public DataEntitySearcherViewRep getDataEntitySearcher();
	
	public PickingManager getPickingManager();
	
	public SelectionManager getSelectionManager();
}
