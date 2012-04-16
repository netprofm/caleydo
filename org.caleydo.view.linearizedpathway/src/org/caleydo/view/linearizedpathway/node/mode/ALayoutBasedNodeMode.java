/**
 * 
 */
package org.caleydo.view.linearizedpathway.node.mode;

import gleem.linalg.Vec3f;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.PixelGLConverter;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.view.linearizedpathway.GLLinearizedPathway;

/**
 * Base class for all nodes that make use of layouts.
 * 
 * @author Christian
 * 
 */
public abstract class ALayoutBasedNodeMode extends ALinearizeableNodeMode {

	protected LayoutManager layoutManager;
	
	protected PixelGLConverter pixelGLConverter;

	/**
	 * @param view
	 */
	public ALayoutBasedNodeMode(GLLinearizedPathway view) {
		super(view);
		this.pixelGLConverter = view.getPixelGLConverter();
		layoutManager = new LayoutManager(new ViewFrustum(), pixelGLConverter);
	}
	
	@Override
	public void render(GL2 gl, GLU glu) {
		float width = pixelGLConverter.getGLWidthForPixelWidth(node.getWidthPixels());
		float height = pixelGLConverter.getGLHeightForPixelHeight(node.getHeightPixels());
		
		Vec3f position = node.getPosition();

		gl.glPushMatrix();
		gl.glTranslatef(position.x() - width / 2.0f, position.y() - height / 2.0f, 0f);
		layoutManager.setViewFrustum(new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC,
				0, width, 0, height, -1, 20));

		layoutManager.render(gl);
		gl.glPopMatrix();

	}

}
