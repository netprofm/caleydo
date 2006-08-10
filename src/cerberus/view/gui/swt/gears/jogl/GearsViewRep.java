package cerberus.view.gui.swt.gears.jogl;

import javax.media.opengl.GLCanvas;

import com.sun.opengl.util.Animator;

import cerberus.manager.GeneralManager;
import cerberus.manager.type.ManagerObjectType;
import cerberus.view.gui.AViewRep;
import cerberus.view.gui.ViewInter;
import cerberus.view.gui.awt.jogl.GearsMain;
import cerberus.view.gui.awt.jogl.TriangleMain;
import cerberus.view.gui.awt.jogl.Histogram2DMain;
import cerberus.view.gui.swt.widget.SWTEmbeddedJoglWidget;

public class GearsViewRep 
extends AViewRep 
implements ViewInter
{
	protected final int iNewId;
	protected GeneralManager refGeneralManager;
	protected GLCanvas refGLCanvas;
	
	public GearsViewRep(int iNewId, GeneralManager refGeneralManager)
	{
		this.iNewId = iNewId;
		this.refGeneralManager = refGeneralManager;
		
		//FIXME: do the following code in a method
		initView();
		retrieveNewGUIContainer();
		drawView();
	}
	
	public void initView()
	{
		// TODO Auto-generated method stub
		
	}

	public void drawView()
	{
		Histogram2DMain canvas = new Histogram2DMain();
		
		/**
		 * Calling "canvas.runMain();" starts a new thread an a new AWT-Frame
		 */
		//canvas.runMain();
		
		refGLCanvas.addGLEventListener( canvas );
		
		/**
		 * old code
		 */
		//refGLCanvas.addGLEventListener( new TriangleMain() );
		
	    //refGLCanvas.addGLEventListener(new GearsMain());

	    final Animator animator = new Animator(refGLCanvas);
	    animator.start();
		
	}

	public void retrieveNewGUIContainer()
	{
		SWTEmbeddedJoglWidget refSWTEmbeddedJoglWidget = 
			(SWTEmbeddedJoglWidget)refGeneralManager.getSingelton()
		.getSWTGUIManager().createWidget(ManagerObjectType.GUI_SWT_EMBEDDED_JOGL_WIDGET);

		refGLCanvas = refSWTEmbeddedJoglWidget.getGLCanvas();
		
	}

	public void retrieveExistingGUIContainer()
	{
		// TODO Auto-generated method stub
		
	}

}
