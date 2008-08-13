package org.caleydo.core.parser.xml.sax.handler.pathway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.caleydo.core.data.graph.pathway.item.edge.PathwayReactionEdgeGraphItem;
import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItemRep;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IXmlParserManager;
import org.caleydo.core.manager.specialized.genome.pathway.EPathwayDatabaseType;
import org.caleydo.core.parser.xml.sax.handler.AXmlParserHandler;
import org.caleydo.core.parser.xml.sax.handler.IXmlParserHandler;
import org.caleydo.core.util.system.StringConversionTool;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraph;
import org.caleydo.util.graph.IGraphItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * XML Parser that is able to load KEGG pathway files. The KEGG XML files follow
 * the KGML. The class triggers the calls in the PathwayManager that actually
 * creates the pathway graph and the items + item reps.
 * 
 * @author Marc Streit
 */
public class KgmlSaxHandler
	extends AXmlParserHandler
	implements IXmlParserHandler
{
	private Attributes attributes;

	private String sAttributeName = "";

	private HashMap<Integer, IGraphItem> hashKgmlEntryIdToVertexRepId;

	private HashMap<String, IGraphItem> hashKgmlNameToVertexRepId;

	private HashMap<String, IGraphItem> hashKgmlReactionIdToVertexRepId;

	private IGraph currentPathway;

	private IGraphItem currentVertex;

	private ArrayList<IGraphItem> alCurrentVertex;

	private IGraphItem currentReactionSubstrateEdgeRep;

	private IGraphItem currentReactionProductEdgeRep;

	private int iCurrentEntryId;

	/**
	 * Constructor.
	 */
	public KgmlSaxHandler(final IGeneralManager generalManager,
			final IXmlParserManager xmlParserManager)
	{

		super(generalManager, xmlParserManager);

		hashKgmlEntryIdToVertexRepId = new HashMap<Integer, IGraphItem>();
		hashKgmlNameToVertexRepId = new HashMap<String, IGraphItem>();
		hashKgmlReactionIdToVertexRepId = new HashMap<String, IGraphItem>();

		alCurrentVertex = new ArrayList<IGraphItem>();

		setXmlActivationTag("pathway");
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String sSimpleName, String sQualifiedName,
			Attributes attributes) throws SAXException
	{

		String sElementName = sSimpleName;
		this.attributes = attributes;

		if ("".equals(sElementName))
		{
			sElementName = sQualifiedName; // namespaceAware = false
		}

		if (attributes != null)
		{
			if (sElementName.equals("pathway"))
				handlePathwayTag();
			else if (sElementName.equals("entry"))
				handleEntryTag();
			else if (sElementName.equals("graphics"))
				handleGraphicsTag();
			else if (sElementName.equals("relation"))
				handleRelationTag();
			// else if (sElementName.equals("subtype"))
			// handleSubtypeTag();
			else if (sElementName.equals("reaction"))
				handleReactionTag();
			else if (sElementName.equals("product"))
				handleReactionProductTag();
			else if (sElementName.equals("substrate"))
				handleReactionSubstrateTag();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String sSimpleName, String sQualifiedName)
			throws SAXException
	{

		String eName = ("".equals(sSimpleName)) ? sQualifiedName : sSimpleName;

		if (null != eName)
		{
			if (eName.equals(sOpeningTag))
			{
				/**
				 * section (xml block) finished, call callback function from
				 * IXmlParserManager
				 */
				xmlParserManager.sectionFinishedByHandler(this);
			}
		}
	}

	/**
	 * Reacts on the elements of the pathway tag. An example pathway tag looks
	 * like this: <pathway name="path:map00271" org="map" number="00271"
	 * title="Methionine metabolism"
	 * image="http://www.genome.jp/kegg/pathway/map/map00271.gif"
	 * link="http://www.genome.jp/dbget-bin/show_pathway?map00271">
	 */
	protected void handlePathwayTag()
	{

		String sName = "";
		String sTitle = "";
		String sImageLink = "";
		String sExternalLink = "";
		// int iKeggId = 0;

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("name"))
			{
				sName = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("title"))
			{
				sTitle = attributes.getValue(iAttributeIndex);
			}
			// else if (sAttributeName.equals("number"))
			// {
			// iKeggId = new Integer(attributes.getValue(iAttributeIndex));
			// }
			else if (sAttributeName.equals("image"))
			{
				sImageLink = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("link"))
			{
				sExternalLink = attributes.getValue(iAttributeIndex);
			}
		}

		if (sTitle.length() == 0)
			sTitle = "unknown title";

		// Find out pathway texture width and height
		// TODO: Find faster solution for extracting width and height
		String sPathwayTexturePath = sImageLink.substring(sImageLink.lastIndexOf('/') + 1,
				sImageLink.length());

		currentPathway = generalManager.getPathwayManager().createPathway(
				EPathwayDatabaseType.KEGG, sName, sTitle, sPathwayTexturePath, sExternalLink);
	}

	/**
	 * Reacts on the elements of the entry tag. An example entry tag looks like
	 * this: <entry id="1" name="ec:1.8.4.1" type="enzyme" reaction="rn:R01292"
	 * link="http://www.genome.jp/dbget-bin/www_bget?enzyme+1.8.4.1">
	 */
	protected void handleEntryTag()
	{

		int iEntryId = 0;
		String sName = "";
		String sType = "";
		String sExternalLink = "";
		String sReactionId = "";

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("id"))
			{
				iEntryId = new Integer(attributes.getValue(iAttributeIndex));
			}
			else if (sAttributeName.equals("name"))
			{
				sName = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("type"))
			{
				sType = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("link"))
			{
				sExternalLink = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("reaction"))
			{
				sReactionId = attributes.getValue(iAttributeIndex);
			}
		}

		iCurrentEntryId = iEntryId;
		alCurrentVertex.clear();

		if (sType.equals("gene"))
		{
			StringTokenizer sTokenText = new StringTokenizer(sName, " ");
			int iDavidId = -1;
			String sTmpVertexName = "";

			while (sTokenText.hasMoreTokens())
			{
				sTmpVertexName = sTokenText.nextToken();

				iDavidId = generalManager.getGenomeIdManager().getIdIntFromIntByMapping(
						StringConversionTool.convertStringToInt(sTmpVertexName.substring(4),
								-1), EMappingType.ENTREZ_GENE_ID_2_DAVID);

				if (iDavidId == -1)
				{
					generalManager.getLogger().log(
							Level.WARNING,
							"NCBI Gene ID " + sTmpVertexName
									+ " cannot be mapped to David Id.");

					continue;
				}

				currentVertex = generalManager.getPathwayItemManager().createVertexGene(
						sTmpVertexName, sType, sExternalLink, sReactionId, iDavidId);

				alCurrentVertex.add(currentVertex);
			}
		}
		else
		{
			currentVertex = generalManager.getPathwayItemManager().createVertex(sName, sType,
					sExternalLink, sReactionId);

			alCurrentVertex.add(currentVertex);
		}
	}

	/**
	 * Reacts on the elements of the graphics tag. An example graphics tag looks
	 * like this: <graphics name="1.8.4.1" fgcolor="#000000" bgcolor="#FFFFFF"
	 * type="rectangle" x="142" y="304" width="45" height="17"/>
	 */
	protected void handleGraphicsTag()
	{

		String sName = "";
		String sShapeType = "";
		short shHeight = 0;
		short shWidth = 0;
		short shXPosition = 0;
		short shYPosition = 0;

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("name"))
			{
				sName = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("height"))
			{
				shHeight = new Short(attributes.getValue(iAttributeIndex));
			}
			else if (sAttributeName.equals("width"))
			{
				shWidth = new Short(attributes.getValue(iAttributeIndex));
			}
			else if (sAttributeName.equals("x"))
			{
				shXPosition = new Short(attributes.getValue(iAttributeIndex));
			}
			else if (sAttributeName.equals("y"))
			{
				shYPosition = new Short(attributes.getValue(iAttributeIndex));
			}
			else if (sAttributeName.equals("type"))
			{
				sShapeType = attributes.getValue(iAttributeIndex);
			}
		}

		IGraphItem vertexRep = generalManager.getPathwayItemManager().createVertexRep(
				currentPathway, alCurrentVertex, sName, sShapeType, shXPosition, shYPosition,
				shWidth, shHeight);

		hashKgmlEntryIdToVertexRepId.put(iCurrentEntryId, vertexRep);
		hashKgmlNameToVertexRepId.put(((PathwayVertexGraphItem) currentVertex).getName(),
				vertexRep);
		hashKgmlReactionIdToVertexRepId.put(((PathwayVertexGraphItem) currentVertex)
				.getReactionId(), vertexRep);
	}

	/**
	 * Reacts on the elements of the relation tag. An example relation tag looks
	 * like this: <relation entry1="28" entry2="32" type="ECrel">
	 */
	protected void handleRelationTag()
	{

		int iSourceVertexId = 0;
		int iTargetVertexId = 0;
		String sType = "";

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("type"))
				sType = attributes.getValue(iAttributeIndex);
			else if (sAttributeName.equals("entry1"))
				iSourceVertexId = new Integer(attributes.getValue(iAttributeIndex));
			else if (sAttributeName.equals("entry2"))
				iTargetVertexId = new Integer(attributes.getValue(iAttributeIndex));

			// System.out.println("Attribute name: " +sAttributeName);
			// System.out.println("Attribute value: "
			// +attributes.getValue(iAttributeIndex));
		}

		IGraphItem graphItemIn = hashKgmlEntryIdToVertexRepId.get(iSourceVertexId);
		IGraphItem graphItemOut = hashKgmlEntryIdToVertexRepId.get(iTargetVertexId);

		// Create edge (data)
		IGraphItem relationEdge = generalManager.getPathwayItemManager().createRelationEdge(
				((PathwayVertexGraphItemRep) graphItemIn).getPathwayVertexGraphItem(),
				((PathwayVertexGraphItemRep) graphItemOut).getPathwayVertexGraphItem(), sType);

		// Create edge representation
		generalManager.getPathwayItemManager().createRelationEdgeRep(currentPathway,
				relationEdge, graphItemIn, graphItemOut);

	}

	//    	
	// protected void handleSubtypeTag() {
	//    	
	// String sName = "";
	// int iCompoundId = 0;
	//		
	// for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength();
	// iAttributeIndex++)
	// {
	// sAttributeName = attributes.getLocalName(iAttributeIndex);
	//		
	// if ("".equals(sAttributeName))
	// {
	// sAttributeName = attributes.getQName(iAttributeIndex);
	// }
	//				
	// if (sAttributeName.equals("name"))
	// sName = attributes.getValue(iAttributeIndex);
	// else if (sAttributeName.equals("value"))
	// {
	// // TODO: handle special case of value "-->" in signalling pathways
	// if (attributes.getValue(iAttributeIndex).contains("-") ||
	// attributes.getValue(iAttributeIndex).contains("=") ||
	// attributes.getValue(iAttributeIndex).contains("+") ||
	// attributes.getValue(iAttributeIndex).contains(":") ||
	// attributes.getValue(iAttributeIndex).contains("."))
	// iCompoundId = 0;
	// else
	// iCompoundId = new Integer(attributes.getValue(iAttributeIndex));
	// }
	//
	// 
	// //System.out.println("Attribute name: " +sAttributeName);
	// //System.out.println("Attribute value: "
	// +attributes.getValue(iAttributeIndex));
	// }
	//		
	// if (sName.equals("compound"))
	// {
	// //retrieve the internal element ID and add the compound value to the edge
	// generalManager.getSingelton().getPathwayElementManager().
	// addRelationCompound(kgmlIdToElementIdLUT.get(iCompoundId));
	// }
	// }
	//    
	/**
	 * Reacts on the elements of the reaction tag. An example reaction tag looks
	 * like this: <reaction name="rn:R01001" type="irreversible">
	 */
	protected void handleReactionTag()
	{

		String sReactionName = "";
		String sReactionType = "";

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("type"))
			{
				sReactionType = attributes.getValue(iAttributeIndex);
			}
			else if (sAttributeName.equals("name"))
			{
				sReactionName = attributes.getValue(iAttributeIndex);
			}
		}

		currentReactionSubstrateEdgeRep = generalManager.getPathwayItemManager()
				.createReactionEdge(currentPathway, sReactionName, sReactionType);

		currentReactionProductEdgeRep = generalManager.getPathwayItemManager()
				.createReactionEdge(currentPathway, sReactionName, sReactionType);

	}

	/**
	 * Reacts on the elements of the reaction substrate tag. An example reaction
	 * substrate tag looks like this: <substrate name="cpd:C01118"/>
	 */
	protected void handleReactionSubstrateTag()
	{

		String sReactionSubstrateName = "";

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("name"))
			{
				sReactionSubstrateName = attributes.getValue(iAttributeIndex);
			}
		}

		IGraphItem graphItemIn = hashKgmlNameToVertexRepId.get(sReactionSubstrateName);

		IGraphItem graphItemOut = hashKgmlReactionIdToVertexRepId
				.get(((PathwayReactionEdgeGraphItem) currentReactionSubstrateEdgeRep
						.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).toArray()[0])
						.getReactionId());

		if (graphItemIn == null || graphItemOut == null)
		{
			return;
		}

		currentReactionSubstrateEdgeRep.addItemDoubleLinked(graphItemIn,
				EGraphItemProperty.INCOMING);

		currentReactionSubstrateEdgeRep.addItemDoubleLinked(graphItemOut,
				EGraphItemProperty.OUTGOING);

		IGraphItem tmpReactionEdge = (PathwayReactionEdgeGraphItem) currentReactionSubstrateEdgeRep
				.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).toArray()[0];

		tmpReactionEdge.addItemDoubleLinked((IGraphItem) graphItemIn.getAllItemsByProp(
				EGraphItemProperty.ALIAS_PARENT).toArray()[0], EGraphItemProperty.INCOMING);

		tmpReactionEdge.addItemDoubleLinked((IGraphItem) graphItemOut.getAllItemsByProp(
				EGraphItemProperty.ALIAS_PARENT).toArray()[0], EGraphItemProperty.OUTGOING);
	}

	/**
	 * Reacts on the elements of the reaction product tag. An example reaction
	 * product tag looks like this: <product name="cpd:C02291"/>
	 */
	protected void handleReactionProductTag()
	{

		String sReactionProductName = "";

		for (int iAttributeIndex = 0; iAttributeIndex < attributes.getLength(); iAttributeIndex++)
		{
			sAttributeName = attributes.getLocalName(iAttributeIndex);

			if ("".equals(sAttributeName))
			{
				sAttributeName = attributes.getQName(iAttributeIndex);
			}

			if (sAttributeName.equals("name"))
			{
				sReactionProductName = attributes.getValue(iAttributeIndex);
			}
		}

		// Compound
		IGraphItem graphItemOut = hashKgmlNameToVertexRepId.get(sReactionProductName);

		// Enzyme
		IGraphItem graphItemIn = hashKgmlReactionIdToVertexRepId
				.get(((PathwayReactionEdgeGraphItem) currentReactionProductEdgeRep
						.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).toArray()[0])
						.getReactionId());

		if (graphItemIn == null || graphItemOut == null)
		{
			return;
		}

		currentReactionProductEdgeRep.addItemDoubleLinked(graphItemIn,
				EGraphItemProperty.INCOMING);

		currentReactionProductEdgeRep.addItemDoubleLinked(graphItemOut,
				EGraphItemProperty.OUTGOING);

		IGraphItem tmpReactionEdge = (PathwayReactionEdgeGraphItem) currentReactionProductEdgeRep
				.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).toArray()[0];

		tmpReactionEdge.addItemDoubleLinked((IGraphItem) graphItemIn.getAllItemsByProp(
				EGraphItemProperty.ALIAS_PARENT).toArray()[0], EGraphItemProperty.INCOMING);

		tmpReactionEdge.addItemDoubleLinked((IGraphItem) graphItemOut.getAllItemsByProp(
				EGraphItemProperty.ALIAS_PARENT).toArray()[0], EGraphItemProperty.OUTGOING);
	}

	/**
	 * @see org.caleydo.core.parser.handler.IXmlParserHandler#destroyHandler()
	 * @see org.caleydo.core.parser.handler.AXmlParserHandler#destroyHandler()
	 */
	public void destroyHandler()
	{

		super.destroyHandler();

		hashKgmlEntryIdToVertexRepId.clear();
		hashKgmlNameToVertexRepId.clear();
		hashKgmlReactionIdToVertexRepId.clear();
	}
}
