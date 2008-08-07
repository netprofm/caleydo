package org.caleydo.core.manager.specialized.glyph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.logging.Level;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.type.EManagerType;
import org.caleydo.core.view.opengl.canvas.glyph.GLCanvasGlyph;
import org.caleydo.core.view.opengl.canvas.glyph.GLCanvasGlyphGenerator;
import org.caleydo.core.view.opengl.canvas.glyph.GlyphAttributeType;
import org.caleydo.core.view.opengl.canvas.glyph.GlyphEntry;

/**
 * @author Sauer Stefan
 */
public class GlyphManager
	extends AManager<GlyphEntry>
	implements IGlyphManager
{

	
	private HashMap<EGlyphSettingIDs, String> settings;

	private Vector<Integer> sortOrderExt;

	private GLCanvasGlyphGenerator generator = null;

	private HashMap<Integer, GlyphAttributeType> dataTypesExt = null;

//	private HashMap<Integer, GlyphEntry> hmGlyphList = null;

	private HashSet<GLCanvasGlyph> registeredViews = null;

	/**
	 * Constructor.
	 */
	public GlyphManager(final IGeneralManager generalManager)
	{

		super(generalManager, IGeneralManager.iUniqueId_TypeOffset_Pathways_Pathway,
				EManagerType.DATA_PATHWAY_ELEMENT);

		settings = new HashMap<EGlyphSettingIDs, String>();
		sortOrderExt = new Vector<Integer>();

		generator = new GLCanvasGlyphGenerator();
		dataTypesExt = new HashMap<Integer, GlyphAttributeType>();
		registeredViews = new HashSet<GLCanvasGlyph>();
	}

	public void registerGlyphView(GLCanvasGlyph view)
	{

		if (!registeredViews.contains(view))
			registeredViews.add(view);
	}

	public void unregisterGlyphView(GLCanvasGlyph view)
	{

		if (!registeredViews.contains(view))
			registeredViews.remove(view);
	}

	public void loadGlyphDefinitaion(String xmlPath)
	{

		generalManager.getLogger().log(Level.INFO, "loadGlyphDefinitaion");
		generalManager.getXmlParserManager().parseXmlFileByName(xmlPath);

	}

	// settings accessors
	public String getSetting(EGlyphSettingIDs type)
	{

		if (settings.containsKey(type))
			return settings.get(type);
		return null;
	}

	public void setSetting(EGlyphSettingIDs type, String value)
	{

		if (settings.containsKey(type))
			settings.remove(type);
		settings.put(type, value);

		initGlyphGenerator();

		for (GLCanvasGlyph v : registeredViews)
			v.forceRebuild();
	}

	public int getSortOrder(int depth)
	{

		if (sortOrderExt.size() > depth)
		{
			Integer extindex = sortOrderExt.get(depth);
			if (dataTypesExt.containsKey(extindex))
				return dataTypesExt.get(extindex).getInternalColumnNumber();
		}
		return -1;
	}

	public void addSortColumn(String value)
	{

		int x = Integer.parseInt(value);
		sortOrderExt.add(x);
	}

	public void addColumnAttributeType(GlyphAttributeType type)
	{

		int index = type.getExternalColumnNumber();
		if (dataTypesExt.containsKey(index))
		{
			dataTypesExt.remove(index);
			generalManager
					.getLogger()
					.log(Level.WARNING,
							"GlyphManager::addColumnAttributeType() - double column definition, dropping first one");
		}
		dataTypesExt.put(index, type);
	}

	public Collection<GlyphAttributeType> getGlyphAttributes()
	{

		return dataTypesExt.values();
	}

	public GlyphAttributeType getGlyphAttributeTypeWithExternalColumnNumber(int colnum)
	{

		if (dataTypesExt.containsKey(colnum))
			return dataTypesExt.get(colnum);
		return null;
	}

	public GlyphAttributeType getGlyphAttributeTypeWithInternalColumnNumber(int colnum)
	{

		for (GlyphAttributeType t : dataTypesExt.values())
			if (t.getInternalColumnNumber() == colnum)
				return t;
		return null;
	}

	public GLCanvasGlyphGenerator getGlyphGenerator()
	{

		return generator;
	}

	public void initGlyphGenerator()
	{

		try
		{
			// init indices
			int ebtc = Integer.parseInt(getSetting(EGlyphSettingIDs.TOPCOLOR));
			GlyphAttributeType typ = getGlyphAttributeTypeWithExternalColumnNumber(ebtc);
			if (typ != null)
			{
				int ibtc = typ.getInternalColumnNumber();
				generator.setIndexTopColor(ibtc);
			}
			ebtc = Integer.parseInt(getSetting(EGlyphSettingIDs.BOXCOLOR));
			typ = getGlyphAttributeTypeWithExternalColumnNumber(ebtc);
			if (typ != null)
			{
				int ibtc = typ.getInternalColumnNumber();
				generator.setIndexBoxColor(ibtc);
			}
			ebtc = Integer.parseInt(getSetting(EGlyphSettingIDs.BOXHEIGHT));
			typ = getGlyphAttributeTypeWithExternalColumnNumber(ebtc);
			if (typ != null)
			{
				int ibtc = typ.getInternalColumnNumber();
				generator.setIndexHeight(ibtc);
			}

			// set max height value
			ebtc = Integer.parseInt(getSetting(EGlyphSettingIDs.BOXHEIGHT));
			int maxHeight = getGlyphAttributeTypeWithExternalColumnNumber(ebtc).getMaxIndex();
			generator.setMaxHeight(maxHeight);

		}
		catch (Exception ex)
		{
			this.generalManager.getLogger().log(
					Level.WARNING,
					"GlyphManager::initGlyphGenerator() - parsing integer failed!\r\n"
							+ ex.getMessage());
		}
	}

	public void addGlyph(int id, GlyphEntry glyph)
	{
		hashItems.put(id, glyph);
	}

	public void addGlyphs(HashMap<Integer, GlyphEntry> glyphlist)
	{
		hashItems.putAll(glyphlist);
	}



	public HashMap<Integer, GlyphEntry> getGlyphs()
	{
		return hashItems;
	}

}
