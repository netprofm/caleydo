package org.caleydo.core.view.opengl.renderstyle;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.EventPublisher;
import org.caleydo.core.manager.event.SetMinViewSizeEvent;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;

/**
 * Render Styles for the whole system
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class GeneralRenderStyle {

	public static final float VERY_SMALL_FONT_SCALING_FACTOR = 0.002f;

	public static final float SMALL_FONT_SCALING_FACTOR = 0.003f;

	public static final float HEADING_FONT_SCALING_FACTOR = 0.005f;

	public static final float INFO_AREA_Z = 0.02f;

	public static final float INFO_AREA_CONNECTION_Z = 0.01f;

	public static final float MINIVEW_Z = 0.02f;

	private static final float[] BACKGROUND_COLOR = { 0.7f, 0.7f, 0.7f, 1f };

	public static final float[] PANEL_BACKGROUN_COLOR = { 0.85f, 0.85f, 0.85f, 1f };

	// public static final float [] MENU_ITEM_COLOR = {0.}

	public static final float LOADING_BOX_HALF_WIDTH = 1f;

	public static final float LOADING_BOX_HALF_HEIGHT = 0.3f;

	public static final float SELECTED_LINE_WIDTH = 2;

	public static final float MOUSE_OVER_LINE_WIDTH = 2;

	public static final int NUM_CHAR_LIMIT = 8;

	protected static final float BUTTONS_SPACING = 0.005f;

	protected static final float BUTTON_WIDTH = 0.018f;

	protected IViewFrustum viewFrustum;

	/** The selection type for which vislinks should be rendered */
	public static final SelectionType VISLINK_SELECTION_TYPE = SelectionType.SELECTION;

	protected int minViewWidth;
	protected int minViewHeight;

	/**
	 * Default constructor.
	 */
	private GeneralRenderStyle() {
		minViewWidth = 0;
		minViewHeight = 0;
	}

	/**
	 * Constructor.
	 */
	public GeneralRenderStyle(IViewFrustum viewFrustum) {
		this();
		this.viewFrustum = viewFrustum;
		minViewWidth = 0;
		minViewHeight = 0;
	}

	public float getSmallFontScalingFactor() {
		float fScaling = SMALL_FONT_SCALING_FACTOR;
		return fScaling;
	}

	public float getVerySmallFontScalingFactor() {

		return VERY_SMALL_FONT_SCALING_FACTOR;// * getScaling();
	}

	public float getHeadingFontScalingFactor() {

		return HEADING_FONT_SCALING_FACTOR;// * getScaling();
	}

	public float getVerySmallSpacing() {

		return BUTTONS_SPACING / 5 * getScaling();
	}

	public float getSmallSpacing() {

		return BUTTONS_SPACING * getScaling();
	}

	public float getButtonWidht() {

		return BUTTON_WIDTH * getScaling();
	}

	public float getScaling() {
		float fScaling;
		if (viewFrustum.getWidth() > viewFrustum.getHeight()) {
			fScaling = viewFrustum.getWidth();
		}
		else {
			fScaling = viewFrustum.getHeight();
		}
		return fScaling;
	}

	public float[] getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	public int getMinViewWidth() {
		return minViewWidth;
	}

	public void setMinViewWidth(int minViewWidth, AGLView view) {
		this.minViewWidth = minViewWidth;

		sendSetMinSizeEvent(view);
	}

	public int getMinViewHeight() {
		return minViewHeight;
	}

	public void setMinViewHeight(int minViewHeight, AGLView view) {
		this.minViewHeight = minViewHeight;

		sendSetMinSizeEvent(view);
	}

	public void setMinViewDimensions(int minViewWidth, int minViewHeight, AGLView view) {
		this.minViewHeight = minViewHeight;
		this.minViewWidth = minViewWidth;

		sendSetMinSizeEvent(view);
	}

	private void sendSetMinSizeEvent(AGLView view) {
		EventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
		SetMinViewSizeEvent event = new SetMinViewSizeEvent();
		event.setMinViewSize(minViewWidth, minViewHeight);
		event.setView(view);
		eventPublisher.triggerEvent(event);
	}

}
