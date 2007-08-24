package cerberus.data.graph.item.vertex;

import org.geneview.graph.EGraphItemKind;
import org.geneview.graph.item.GraphItem;


public class PathwayVertexGraphItem 
extends GraphItem {

	final String sName;
	
	final EPathwayVertexType type;
	
	final String sExternalLink;
	
	final String sReactionId;
	
	public PathwayVertexGraphItem(
			final int iId,
			final String sName,
			final String sType,
			final String sExternalLink,
			final String sReactionId) {
		
		super(iId, EGraphItemKind.NODE);

		type = EPathwayVertexType.valueOf(sType);
		
		this.sName = sName;
		this.sExternalLink = sExternalLink;
		this.sReactionId = sReactionId;
	}
	
	public String getName() {
		
		return sName;
	}
	
	public EPathwayVertexType getType() {
		
		return type;
	}
	
	public String getExternalLink() {
		
		return sExternalLink;
	}
	
	public String getReactionId() {
		
		return sReactionId;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return sName;
	}
}
