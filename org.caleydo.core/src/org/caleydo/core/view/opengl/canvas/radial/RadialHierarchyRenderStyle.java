package org.caleydo.core.view.opengl.canvas.radial;

import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;

/**
 * Radial render styles
 */

public class RadialHierarchyRenderStyle
	extends GeneralRenderStyle {
	
	public static float[] PARTIAL_DISC_BORDER_COLOR = {1, 1, 1, 1};
	public static float[] PARTIAL_DISC_ROOT_COLOR = {1, 1, 1, 1};
	public static float[] PARTIAL_DISC_MOUSE_OVER_COLOR = {0.6f, 0.8f, 0.8f, 1.0f};
	public static float PARTIAL_DISC_BORDER_WIDTH = 2;
	public static int NUM_SLICES_PER_FULL_DISC = 100;
	
	public static float[] CHILD_INDICATOR_COLOR = {0.3f, 0.3f, 0.3f, 1.0f};
	public static final float TRIANGLE_HEIGHT_PERCENTAGE = 0.03f;
	public static final float TRIANGLE_TOP_RADIUS_PERCENTAGE = 1.025f;
	
	

	public RadialHierarchyRenderStyle(IViewFrustum viewFrustum) {
		super(viewFrustum);
	}

}
