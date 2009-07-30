package org.caleydo.core.view.opengl.canvas.hyperbolic;

import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.hyperbolic.graphnodes.ADrawableNode;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;

/**
 * Hyperbolic render styles
 */

public class HyperbolicRenderStyle
	extends GeneralRenderStyle {

	public static final float X_BORDER_SPACING = 0.7f;
	public static final float Y_BORDER_SPACING = 0.7f;

	public static final float SIDE_SPACING = 0.1f;

	public static final float[] DA_OBJ_FALLBACK_COLORSCHEME = { 0.0f, 0.0f, 0.0f };
	public static final float DA_OBJ_FALLBACK_ALPHA = 1;

	public static final float[] DA_OBJ_SQUARE_COLORSCHEME = { 0.5f, 0.5f, 0.5f };
	public static final float DA_OBJ_SQUARE_ALPHA = 1;
	
	// Define detail level objects for the Test Node implementation
	public static enum DA_TEST_NODE_DL_OBJ {
		VeryHigh("Square"),
		High("Fallback"),
		Normal("Fallback"),
		Low("Fallback"),
		VeryLow("Fallback");
		private final String name;

		DA_TEST_NODE_DL_OBJ(String str) {
			this.name = str;
		}

		public String toString() {
			return name;
		}
	}

	/**
	 * Specify how many contact points a DA object should return. value must be a multiple of 4 (4, 8, 12,
	 * 16,...)
	 */
	public static final int DA_OBJ_NUM_CONTACT_POINTS = 16;

	public HyperbolicRenderStyle(IViewFrustum viewFrustum) {
		super(viewFrustum);
	}

}
