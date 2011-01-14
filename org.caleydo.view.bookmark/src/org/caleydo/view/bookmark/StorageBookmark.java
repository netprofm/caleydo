package org.caleydo.view.bookmark;

import javax.media.opengl.GL2;

import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;

/**
 * A bookmark for a gene. The id used here is DAVID
 * 
 * @author Alexander Lex
 */
class StorageBookmark extends ABookmark {

	/**
	 * Constructor taking a textRenderer
	 * 
	 * @param textRenderer
	 * @param davidID
	 */
	public StorageBookmark(GLBookmarkView manager, IDType idType,
			Integer experimentIndex, CaleydoTextRenderer textRenderer) {
		super(manager, idType, textRenderer);
		this.id = experimentIndex;
		bookmarkDimensions.setHeight(0.1f);
	}

	@Override
	public void render(GL2 gl) {
		String sContent = manager.getDataDomain().getStorageLabel(id);

		// ((ISetBasedDataDomain) DataDomainManager.getInstance()
		// .getDataDomain("org.caleydo.datadomain.genetic")).getSet().get(id).getLabel();
		//
		// GeneralManager.get().getIDMappingManager().getID(EIDType.DAVID,
		// EIDType.GENE_SYMBOL, id);
		//
		float yOrigin = bookmarkDimensions.getYOrigin() - 0.08f;
		RenderingHelpers.renderText(gl, textRenderer, sContent, bookmarkDimensions.getXOrigin()
				+ BookmarkRenderStyle.SIDE_SPACING * 2, yOrigin,
				GeneralRenderStyle.SMALL_FONT_SCALING_FACTOR);

	}

}
