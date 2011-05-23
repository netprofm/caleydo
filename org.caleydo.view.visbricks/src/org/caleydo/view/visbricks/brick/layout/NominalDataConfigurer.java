package org.caleydo.view.visbricks.brick.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.visbricks.brick.EContainedViewType;
import org.caleydo.view.visbricks.brick.GLBrick;
import org.caleydo.view.visbricks.brick.ui.AContainedViewRenderer;
import org.caleydo.view.visbricks.brick.ui.BrickRemoteViewRenderer;
import org.caleydo.view.visbricks.brick.ui.BrickViewSwitchingButton;
import org.caleydo.view.visbricks.brick.viewcreation.ParCoordsCreator;
import org.caleydo.view.visbricks.brick.viewcreation.TagCloudCreator;

public class NominalDataConfigurer extends ASetBasedDataConfigurer {
	
	protected static final int PARCOORDS_BUTTON_ID = 2;
	protected static final int TAGCLOUD_BUTTON_ID = 3;
	
	public NominalDataConfigurer(ISet set) {
		super(set);
	}

	

	@Override
	public void configure(CentralBrickLayoutTemplate layoutTemplate) {
		BrickViewSwitchingButton parCoordsButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				PARCOORDS_BUTTON_ID, EIconTextures.PAR_COORDS_ICON,
				EContainedViewType.PARCOORDS_VIEW);
		BrickViewSwitchingButton tagCloudButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				TAGCLOUD_BUTTON_ID, EIconTextures.TAGCLOUD_ICON,
				EContainedViewType.TAGCLOUD_VIEW);
		
		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(parCoordsButton);
		viewSwitchingButtons.add(tagCloudButton);
		
		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		ArrayList<ElementLayout> toolBarElements = createToolBarElements(
				layoutTemplate, viewSwitchingButtons);
		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		
		layoutTemplate.setHeaderBarElements(headerBarElements);
		layoutTemplate.setToolBarElements(toolBarElements);
		layoutTemplate.setFooterBarElements(footerBarElements);

//		layoutTemplate.setViewSwitchingButtons(viewSwitchingButtons);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.PARCOORDS_VIEW);
		validViewTypes.add(EContainedViewType.TAGCLOUD_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.PARCOORDS_VIEW);
		
		layoutTemplate.showToolBar(true);
		layoutTemplate.showFooterBar(true);
	}
	
	

	@Override
	public void configure(CompactBrickLayoutTemplate layoutTemplate) {
		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.TAGCLOUD_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.TAGCLOUD_VIEW);
		
		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);
	}

	@Override
	public void configure(DefaultBrickLayoutTemplate layoutTemplate) {

		BrickViewSwitchingButton parCoordsButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				PARCOORDS_BUTTON_ID, EIconTextures.PAR_COORDS_ICON,
				EContainedViewType.PARCOORDS_VIEW);
		BrickViewSwitchingButton tagCloudButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				TAGCLOUD_BUTTON_ID, EIconTextures.TAGCLOUD_ICON,
				EContainedViewType.TAGCLOUD_VIEW);
		
		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(parCoordsButton);
		viewSwitchingButtons.add(tagCloudButton);
		
		ArrayList<ElementLayout> toolBarElements = createToolBarElements(
				layoutTemplate, viewSwitchingButtons);
		layoutTemplate.setToolBarElements(toolBarElements);

		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);

//		layoutTemplate.setViewSwitchingButtons(viewSwitchingButtons);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.PARCOORDS_VIEW);
		validViewTypes.add(EContainedViewType.TAGCLOUD_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.PARCOORDS_VIEW);
		
		layoutTemplate.showFooterBar(true);

	}
	
	@Override
	public void configure(DetailBrickLayoutTemplate layoutTemplate) {

		BrickViewSwitchingButton parCoordsButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				PARCOORDS_BUTTON_ID, EIconTextures.PAR_COORDS_ICON,
				EContainedViewType.PARCOORDS_VIEW);
		BrickViewSwitchingButton tagCloudButton = new BrickViewSwitchingButton(
				EPickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS,
				TAGCLOUD_BUTTON_ID, EIconTextures.TAGCLOUD_ICON,
				EContainedViewType.TAGCLOUD_VIEW);
		
		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(parCoordsButton);
		viewSwitchingButtons.add(tagCloudButton);
		
		ArrayList<ElementLayout> toolBarElements = createToolBarElements(
				layoutTemplate, viewSwitchingButtons);
		layoutTemplate.setToolBarElements(toolBarElements);

		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);

//		layoutTemplate.setViewSwitchingButtons(viewSwitchingButtons);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.PARCOORDS_VIEW);
		validViewTypes.add(EContainedViewType.TAGCLOUD_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.PARCOORDS_VIEW);
		
		layoutTemplate.showFooterBar(true);

	}

	@Override
	public void setBrickViews(GLBrick brick, GL2 gl,
			GLMouseListener glMouseListener, ABrickLayoutTemplate brickLayout) {
		
		HashMap<EContainedViewType, AGLView> views = new HashMap<EContainedViewType, AGLView>();
		HashMap<EContainedViewType, AContainedViewRenderer> containedViewRenderers = new HashMap<EContainedViewType, AContainedViewRenderer>();
		
		ParCoordsCreator parCoordsCreator = new ParCoordsCreator(set);
		AGLView parCoords = parCoordsCreator.createRemoteView(brick, gl,
				glMouseListener);
		AContainedViewRenderer parCoordsLayoutRenderer = new BrickRemoteViewRenderer(
				parCoords, brick);
		views.put(EContainedViewType.PARCOORDS_VIEW, parCoords);
		containedViewRenderers.put(EContainedViewType.PARCOORDS_VIEW,
				parCoordsLayoutRenderer);
		
		TagCloudCreator tagCloudCreator = new TagCloudCreator(set);
		AGLView tagCloud = tagCloudCreator.createRemoteView(brick, gl,
				glMouseListener);
		AContainedViewRenderer tagCloudLayoutRenderer = new BrickRemoteViewRenderer(
				tagCloud, brick);
		views.put(EContainedViewType.TAGCLOUD_VIEW, tagCloud);
		containedViewRenderers.put(EContainedViewType.TAGCLOUD_VIEW,
				tagCloudLayoutRenderer);
		

		brick.setViews(views);
		brick.setContainedViewRenderers(containedViewRenderers);
	}

	@Override
	public void configure(CompactCentralBrickLayoutTemplate layoutTemplate) {
		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.TAGCLOUD_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.TAGCLOUD_VIEW);
		
		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		layoutTemplate.setHeaderBarElements(headerBarElements);
		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);
		
		layoutTemplate.showFooterBar(true);
	}


}
