/**
 * 
 */
package org.caleydo.view.linearizedpathway;

import java.util.List;

import org.caleydo.view.linearizedpathway.node.ANode;

/**
 * Class that describes different properties of the spacing between two anchor
 * nodes, i.e. nodes that have associated data rows.
 * 
 * @author Christian
 * 
 */
public class AnchorNodeSpacing {

	/**
	 * First anchor node.
	 */
	private ANode startNode;
	/**
	 * Second anchor node.
	 */
	private ANode endNode;
	/**
	 * Nodes that are inbetween the anchor nodes.
	 */
	private List<ANode> nodesInbetween;
	/**
	 * Minimum spacing between the anchor nodes.
	 */
	private float minAnchorNodeSpacing;

	/**
	 * Current spacing between the anchor nodes.
	 */
	private float currentAnchorNodeSpacing;

	/**
	 * @param startNode
	 *            setter, see {@link #startNode}
	 */
	public void setStartNode(ANode startNode) {
		this.startNode = startNode;
	}

	/**
	 * @return the startNode, see {@link #startNode}
	 */
	public ANode getStartNode() {
		return startNode;
	}

	/**
	 * @param endNode
	 *            setter, see {@link #endNode}
	 */
	public void setEndNode(ANode endNode) {
		this.endNode = endNode;
	}

	/**
	 * @return the endNode, see {@link #endNode}
	 */
	public ANode getEndNode() {
		return endNode;
	}

	/**
	 * @param nodesInbetween
	 *            setter, see {@link #nodesInbetween}
	 */
	public void setNodesInbetween(List<ANode> nodesInbetween) {
		this.nodesInbetween = nodesInbetween;
	}

	/**
	 * @return the nodesInbetween, see {@link #nodesInbetween}
	 */
	public List<ANode> getNodesInbetween() {
		return nodesInbetween;
	}

	/**
	 * @param minAnchorNodeSpacing
	 *            setter, see {@link #minAnchorNodeSpacing}
	 */
	public void setMinAnchorNodeSpacing(float minAnchorNodeSpacing) {
		this.minAnchorNodeSpacing = minAnchorNodeSpacing;
	}

	/**
	 * @return the minAnchorNodeSpacing, see {@link #minAnchorNodeSpacing}
	 */
	public float getMinAnchorNodeSpacing() {
		return minAnchorNodeSpacing;
	}

	/**
	 * @param currentAnchorNodeSpacing
	 *            setter, see {@link #currentAnchorNodeSpacing}
	 */
	public void setCurrentAnchorNodeSpacing(float currentAnchorNodeSpacing) {
		this.currentAnchorNodeSpacing = currentAnchorNodeSpacing;
	}

	/**
	 * @return the currentAnchorNodeSpacing, see
	 *         {@link #currentAnchorNodeSpacing}
	 */
	public float getCurrentAnchorNodeSpacing() {
		return currentAnchorNodeSpacing;
	}

}