/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 * 
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.pathway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.media.opengl.GL2;
import org.caleydo.core.data.IUniqueObject;
import org.caleydo.core.data.collection.dimension.DataRepresentation;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.mapping.IDMappingManager;
import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.GLHelperFunctions;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.edge.PathwayRelationEdgeRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexShape;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.PathwayItemManager;

/**
 * OpenGL2 pathway manager.
 * 
 * @author Marc Streit
 */
public class GLPathwayContentCreator {
	private GeneralManager generalManager;

	private static final float Z_OFFSET = 0.01f;

	private GLPathway glPathwayView;

	private int enzymeNodeDisplayListId = -1;
	private int compoundNodeDisplayListId = -1;
	private int framedEnzymeNodeDisplayListId = -1;
	private int framedCompoundNodeDisplayListId = -1;
	private int upscaledFilledEnzymeNodeDisplayListId = -1;
	private int upscaledFramedEnzymeNodeDisplayListID = -1;

	private boolean enableEdgeRendering = false;
	private boolean enableGeneMapping = true;

	private HashMap<PathwayGraph, Integer> hashPathway2VerticesDisplayListId;
	private HashMap<PathwayGraph, Integer> hashPathway2EdgesDisplayListId;

	private ColorMapper colorMapper;

	private SelectionManager internalSelectionManager;

	private ArrayList<Integer> selectedEdgeRepId;

	private IDMappingManager idMappingManager;

	private PathwayItemManager pathwayItemManager;

	private ATableBasedDataDomain geneticDataDomain;

	private DataRepresentation dimensionDataRepresentation = DataRepresentation.NORMALIZED;

	/**
	 * Constructor.
	 */
	public GLPathwayContentCreator(ViewFrustum viewFrustum, GLPathway glPathwayView) {

		this.generalManager = GeneralManager.get();
		this.glPathwayView = glPathwayView;
		idMappingManager = glPathwayView.getPathwayDataDomain().getGeneIDMappingManager();

		colorMapper = glPathwayView.getDataDomain().getColorMapper();

		hashPathway2VerticesDisplayListId = new HashMap<PathwayGraph, Integer>();
		hashPathway2EdgesDisplayListId = new HashMap<PathwayGraph, Integer>();

		selectedEdgeRepId = new ArrayList<Integer>();

		pathwayItemManager = PathwayItemManager.get();

		geneticDataDomain = glPathwayView.getDataDomain();
	}

	public void init(final GL2 gl, SelectionManager geneSelectionManager) {

		buildEnzymeNodeDisplayList(gl);
		buildCompoundNodeDisplayList(gl);
		buildFramedEnzymeNodeDisplayList(gl);
		buildFramedCompoundNodeDisplayList(gl);
		buildUpscaledEnzymeNodeDisplayList(gl);
		buildUpscaledFramedEnzymeNodeDisplayList(gl);

		this.internalSelectionManager = geneSelectionManager;
	}

	public void buildPathwayDisplayList(final GL2 gl, final IUniqueObject containingView,
			final PathwayGraph pathway) {

		if (pathway == null)
			return;

		int iVerticesDisplayListId = -1;
		int edgesDisplayListId = -1;

		if (hashPathway2VerticesDisplayListId.containsKey(pathway)) {
			// Replace current display list if a display list exists
			iVerticesDisplayListId = hashPathway2VerticesDisplayListId.get(pathway);
		}
		else {
			// Creating vertex display list for pathways
			iVerticesDisplayListId = gl.glGenLists(1);
			hashPathway2VerticesDisplayListId.put(pathway, iVerticesDisplayListId);
		}

		gl.glNewList(iVerticesDisplayListId, GL2.GL_COMPILE);
		extractVertices(gl, containingView, pathway);
		gl.glEndList();

		if (hashPathway2EdgesDisplayListId.containsKey(pathway)) {
			// Replace current display list if a display list exists
			edgesDisplayListId = hashPathway2EdgesDisplayListId.get(pathway);
		}
		else {
			// Creating edge display list for pathways
			edgesDisplayListId = gl.glGenLists(1);
			hashPathway2EdgesDisplayListId.put(pathway, edgesDisplayListId);
		}

		gl.glNewList(edgesDisplayListId, GL2.GL_COMPILE);
		extractEdges(gl, pathway);
		gl.glEndList();
	}

	public void performIdenticalNodeHighlighting(SelectionType selectionType) {
		if (internalSelectionManager == null)
			return;

		selectedEdgeRepId.clear();

		ArrayList<Integer> iAlTmpSelectedGraphItemIds = new ArrayList<Integer>();
		Set<Integer> tmpItemIDs;
		tmpItemIDs = internalSelectionManager.getElements(selectionType);

		if (tmpItemIDs != null) {
			iAlTmpSelectedGraphItemIds.addAll(tmpItemIDs);
		}

		if (iAlTmpSelectedGraphItemIds.size() == 0)
			return;

		// Copy selection IDs to array list object
		for (Integer graphItemID : iAlTmpSelectedGraphItemIds) {

			for (PathwayVertex vertex : pathwayItemManager.getPathwayVertexRep(graphItemID)
					.getPathwayVertices()) {

				for (PathwayVertexRep vertexRep : vertex.getPathwayVertexReps()) {

					if (tmpItemIDs.contains(vertexRep.getID())) {
						continue;
					}
					internalSelectionManager.addToType(selectionType, vertexRep.getID());
				}

			}
		}
	}

	private void buildEnzymeNodeDisplayList(final GL2 gl) {

		enzymeNodeDisplayListId = gl.glGenLists(1);

		float nodeWidth = PathwayRenderStyle.ENZYME_NODE_WIDTH;
		float nodeHeight = PathwayRenderStyle.ENZYME_NODE_HEIGHT;

		gl.glNewList(enzymeNodeDisplayListId, GL2.GL_COMPILE);
		fillNodeDisplayList(gl, nodeWidth, nodeHeight);
		gl.glEndList();
	}

	private void buildUpscaledEnzymeNodeDisplayList(final GL2 gl) {

		upscaledFilledEnzymeNodeDisplayListId = gl.glGenLists(1);

		float nodeWidth = PathwayRenderStyle.ENZYME_NODE_WIDTH;
		float nodeHeight = PathwayRenderStyle.ENZYME_NODE_HEIGHT;

		float scaleFactor = 3;
		nodeWidth *= scaleFactor;
		nodeHeight *= scaleFactor;

		gl.glNewList(upscaledFilledEnzymeNodeDisplayListId, GL2.GL_COMPILE);
		fillNodeDisplayList(gl, nodeWidth, nodeHeight);
		gl.glEndList();
	}

	protected void buildUpscaledFramedEnzymeNodeDisplayList(final GL2 gl) {

		upscaledFramedEnzymeNodeDisplayListID = gl.glGenLists(1);

		float fNodeWidth = PathwayRenderStyle.ENZYME_NODE_WIDTH;
		float fNodeHeight = PathwayRenderStyle.ENZYME_NODE_HEIGHT;

		float scaleFactor = 1.4f;
		fNodeWidth *= scaleFactor;
		fNodeHeight *= scaleFactor;

		gl.glNewList(upscaledFramedEnzymeNodeDisplayListID, GL2.GL_COMPILE);
		fillNodeDisplayListFrame(gl, fNodeWidth, fNodeHeight);
		gl.glEndList();
	}

	protected void buildFramedEnzymeNodeDisplayList(final GL2 gl) {

		framedEnzymeNodeDisplayListId = gl.glGenLists(1);

		float fNodeWidth = PathwayRenderStyle.ENZYME_NODE_WIDTH;
		float fNodeHeight = PathwayRenderStyle.ENZYME_NODE_HEIGHT;

		gl.glNewList(framedEnzymeNodeDisplayListId, GL2.GL_COMPILE);
		fillNodeDisplayListFrame(gl, fNodeWidth, fNodeHeight);
		gl.glEndList();
	}

	protected void buildCompoundNodeDisplayList(final GL2 gl) {
		// Creating display list for node cube objects
		compoundNodeDisplayListId = gl.glGenLists(1);

		float nodeWidth = PathwayRenderStyle.COMPOUND_NODE_WIDTH;
		float nodeHeight = PathwayRenderStyle.COMPOUND_NODE_HEIGHT;

		gl.glNewList(compoundNodeDisplayListId, GL2.GL_COMPILE);
		fillNodeDisplayList(gl, nodeWidth, nodeHeight);
		gl.glEndList();
	}

	protected void buildFramedCompoundNodeDisplayList(final GL2 gl) {
		// Creating display list for node cube objects
		framedCompoundNodeDisplayListId = gl.glGenLists(1);

		float nodeWidth = PathwayRenderStyle.COMPOUND_NODE_WIDTH;
		float nodeHeight = PathwayRenderStyle.COMPOUND_NODE_HEIGHT;

		gl.glNewList(framedCompoundNodeDisplayListId, GL2.GL_COMPILE);
		fillNodeDisplayListFrame(gl, nodeWidth, nodeHeight);
		gl.glEndList();
	}

	private void fillNodeDisplayList(final GL2 gl, float nodeWidth, float nodeHeight) {

		gl.glBegin(GL2.GL_QUADS);
		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(0, 0, Z_OFFSET);
		gl.glVertex3f(nodeWidth, 0, Z_OFFSET);
		gl.glVertex3f(nodeWidth, -nodeHeight, Z_OFFSET);
		gl.glVertex3f(0, -nodeHeight, Z_OFFSET);
		gl.glEnd();
	}

	protected void fillNodeDisplayListFrame(final GL2 gl, float nodeWidth, float nodeHeight) {
		gl.glLineWidth(7);

		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, Z_OFFSET);
		gl.glVertex3f(nodeWidth, 0, Z_OFFSET);
		gl.glVertex3f(nodeWidth, -nodeHeight, Z_OFFSET);
		gl.glVertex3f(0, -nodeHeight, Z_OFFSET);
		gl.glEnd();
	}

	private void extractVertices(final GL2 gl, final IUniqueObject containingView,
			PathwayGraph pathwayToExtract) {

		for (PathwayVertexRep vertexRep : pathwayToExtract.vertexSet()) {
			if (vertexRep == null) {
				continue;
			}

			createVertex(gl, containingView, vertexRep, pathwayToExtract);
		}
	}

	private void extractEdges(final GL2 gl, PathwayGraph pathwayToExtract) {

		// while (pathwayToExtract.edgeSet()) {
		// edgeRep = edgeIterator.next();
		//
		// if (edgeRep != null) {
		// if (enableEdgeRendering) {
		// createEdge(gl, edgeRep, pathwayToExtract);
		// }
		// // Render edge if it is contained in the minimum spanning tree
		// // of the neighborhoods
		// else if (selectedEdgeRepId.contains(edgeRep.getID())) {
		// createEdge(gl, edgeRep, pathwayToExtract);
		// }
		// }
		// }
	}

	private void createVertex(final GL2 gl, final IUniqueObject containingView,
			PathwayVertexRep vertexRep, PathwayGraph containingPathway) {

		float[] tmpNodeColor = null;

		gl.glPushName(generalManager
				.getViewManager()
				.getPickingManager()
				.getPickingID(containingView.getID(),
						PickingType.PATHWAY_ELEMENT_SELECTION.name(), vertexRep.getID()));

		EPathwayVertexShape shape = vertexRep.getShapeType();

		if (shape.equals(EPathwayVertexShape.poly))
			renderPolyVertex(gl, vertexRep);

		float canvasXPos = vertexRep.getCenterX() * PathwayRenderStyle.SCALING_FACTOR_X;
		float canvasYPos = vertexRep.getCenterY() * PathwayRenderStyle.SCALING_FACTOR_Y;
		float nodeWidth = vertexRep.getWidth() * PathwayRenderStyle.SCALING_FACTOR_X;
		float nodeHeight = vertexRep.getHeight() * PathwayRenderStyle.SCALING_FACTOR_Y;

		gl.glTranslatef(canvasXPos, -canvasYPos, 0);

		EPathwayVertexType vertexType = vertexRep.getType();

		switch (vertexType) {

		// Pathway link
			case map:
				// Ignore KEGG title node
				if (vertexRep.getName().contains("TITLE")) {
					gl.glTranslatef(-canvasXPos, canvasYPos, 0);
					gl.glPopName();
					return;
				}

				tmpNodeColor = new float[] { 0f, 0f, 0f, 0.25f };
				gl.glColor4fv(tmpNodeColor, 0);
				fillNodeDisplayList(gl, nodeWidth, nodeHeight);

				// Handle selection highlighting of element

				if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
						vertexRep.getID())) {
					tmpNodeColor = SelectionType.SELECTION.getColor();
					gl.glColor4fv(tmpNodeColor, 0);
					fillNodeDisplayListFrame(gl, nodeWidth, nodeHeight);
				}
				else if (internalSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
						vertexRep.getID())) {
					tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
					gl.glColor4fv(tmpNodeColor, 0);
					fillNodeDisplayListFrame(gl, nodeWidth, nodeHeight);
				}

				break;
			case compound:

				EventBasedSelectionManager metabolicSelectionManager = glPathwayView
						.getMetaboliteSelectionManager();
				// Handle selection highlighting of element
				if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
						vertexRep.getID())
						|| metabolicSelectionManager.checkStatus(SelectionType.SELECTION,
								vertexRep.getName().hashCode())) {
					tmpNodeColor = SelectionType.SELECTION.getColor();

					gl.glColor4fv(tmpNodeColor, 0);
					gl.glCallList(framedCompoundNodeDisplayListId);
				}
				else if (internalSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
						vertexRep.getID())
						|| metabolicSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
								vertexRep.getName().hashCode())) {
					tmpNodeColor = SelectionType.MOUSE_OVER.getColor();

					gl.glColor4fv(tmpNodeColor, 0);
					gl.glCallList(framedCompoundNodeDisplayListId);
				}

				tmpNodeColor = PathwayRenderStyle.COMPOUND_NODE_COLOR;

				gl.glColor4fv(tmpNodeColor, 0);
				gl.glCallList(compoundNodeDisplayListId);

				break;
			case group:

				// gl.glColor4f(1, 1, 0, 1);
				// fillNodeDisplayList(gl, nodeWidth, nodeHeight);
				break;
			case gene:
			case enzyme:
				// new kegg data assign enzymes without mapping to "undefined"
				// which we represent as other
			case other:

				gl.glLineWidth(1);
				if (enableGeneMapping) {

					tmpNodeColor = determineNodeColor(vertexRep);

					if (tmpNodeColor != null) {

						if (glPathwayView.getDetailLevel() == EDetailLevel.HIGH) {

							gl.glColor4f(tmpNodeColor[0], tmpNodeColor[1], tmpNodeColor[2],
									0.7f);

							gl.glEnable(GL2.GL_BLEND);
							gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
							gl.glCallList(enzymeNodeDisplayListId);
							gl.glEnable(GL2.GL_DEPTH_TEST);

							// Handle selection highlighting of element
							if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
									vertexRep.getID())) {
								tmpNodeColor = SelectionType.SELECTION.getColor();
								gl.glColor4fv(tmpNodeColor, 0);
								gl.glCallList(framedEnzymeNodeDisplayListId);
							}
							else if (internalSelectionManager.checkStatus(
									SelectionType.MOUSE_OVER, vertexRep.getID())) {
								tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
								gl.glColor4fv(tmpNodeColor, 0);
								gl.glCallList(framedEnzymeNodeDisplayListId);
							}
						}
						else {
							// Upscaled version of pathway node needed for e.g.
							// VisBricks
							gl.glCallList(upscaledFilledEnzymeNodeDisplayListId);

							// Handle selection highlighting of element
							if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
									vertexRep.getID())) {
								tmpNodeColor = SelectionType.SELECTION.getColor();
								gl.glColor4fv(tmpNodeColor, 0);
								gl.glCallList(upscaledFilledEnzymeNodeDisplayListId);
							}
							else if (internalSelectionManager.checkStatus(
									SelectionType.MOUSE_OVER, vertexRep.getID())) {
								tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
								gl.glColor4fv(tmpNodeColor, 0);
								gl.glCallList(upscaledFilledEnzymeNodeDisplayListId);
							}
						}
					}
					else {
						// render a black glyph in the corder of the
						// rectangle in order to indicate that we either do
						// not have mapping or data

						// transparent node for picking
						gl.glColor4f(0, 0, 0, 0);
						gl.glCallList(enzymeNodeDisplayListId);

						tmpNodeColor = PathwayRenderStyle.ENZYME_NODE_COLOR;
						gl.glColor4f(tmpNodeColor[0], tmpNodeColor[1], tmpNodeColor[2], 0.7f);
						gl.glCallList(compoundNodeDisplayListId);

						// Handle selection highlighting of element
						if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
								vertexRep.getID())) {
							tmpNodeColor = SelectionType.SELECTION.getColor();
							gl.glColor4fv(tmpNodeColor, 0);
							gl.glCallList(framedEnzymeNodeDisplayListId);
						}
						else if (internalSelectionManager.checkStatus(
								SelectionType.MOUSE_OVER, vertexRep.getID())) {
							tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
							gl.glColor4fv(tmpNodeColor, 0);
							gl.glCallList(framedEnzymeNodeDisplayListId);
						}
					}
				}
				else {
					// Handle selection highlighting of element
					if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
							vertexRep.getID())) {
						tmpNodeColor = SelectionType.SELECTION.getColor();
					}
					else if (internalSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
							vertexRep.getID())) {
						tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
					}
					else if (internalSelectionManager.checkStatus(SelectionType.NORMAL,
							vertexRep.getID())) {
						tmpNodeColor = PathwayRenderStyle.ENZYME_NODE_COLOR;
					}
					else {
						tmpNodeColor = new float[] { 0, 0, 0, 0 };
					}

					gl.glColor4fv(tmpNodeColor, 0);
					gl.glCallList(framedEnzymeNodeDisplayListId);

					if (!internalSelectionManager.checkStatus(SelectionType.DESELECTED,
							vertexRep.getID())) {

						// Transparent node for picking
						gl.glColor4f(0, 0, 0, 0);
						gl.glCallList(enzymeNodeDisplayListId);
					}
				}

				break;
		}

		gl.glTranslatef(-canvasXPos, canvasYPos, 0);

		gl.glPopName();
	}

	private void renderPolyVertex(GL2 gl, PathwayVertexRep vertexRep) {

		float[] tmpNodeColor = null;

		ArrayList<Pair<Short, Short>> coords = vertexRep.getCoords();

		gl.glLineWidth(3);
		if (enableGeneMapping) {

			tmpNodeColor = determineNodeColor(vertexRep);
			gl.glLineWidth(4);

			if (tmpNodeColor != null) {
				gl.glColor3fv(tmpNodeColor, 0);

				if (glPathwayView.getDetailLevel() == EDetailLevel.HIGH) {

					gl.glBegin(GL2.GL_LINE_STRIP);
					for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
						gl.glVertex3f(coords.get(pointIndex).getFirst()
								* PathwayRenderStyle.SCALING_FACTOR_X, -coords.get(pointIndex)
								.getSecond() * PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
					}
					gl.glEnd();

					// Transparent node for picking
					gl.glColor4f(0, 0, 0, 0);
					gl.glBegin(GL2.GL_POLYGON);
					for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
						gl.glVertex3f(coords.get(pointIndex).getFirst()
								* PathwayRenderStyle.SCALING_FACTOR_X, -coords.get(pointIndex)
								.getSecond() * PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
					}
					gl.glEnd();
				}
				else {
					gl.glBegin(GL2.GL_POLYGON);
					for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
						gl.glVertex3f(coords.get(pointIndex).getFirst()
								* PathwayRenderStyle.SCALING_FACTOR_X, -coords.get(pointIndex)
								.getSecond() * PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
					}
					gl.glEnd();

					// Handle selection highlighting of element
					if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
							vertexRep.getID())) {
						tmpNodeColor = SelectionType.SELECTION.getColor();
						gl.glLineWidth(3);
						gl.glColor4fv(tmpNodeColor, 0);
						gl.glBegin(GL2.GL_LINE_STRIP);
						for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
							gl.glVertex3f(coords.get(pointIndex).getFirst()
									* PathwayRenderStyle.SCALING_FACTOR_X,
									-coords.get(pointIndex).getSecond()
											* PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
						}
						gl.glEnd();
					}
					else if (internalSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
							vertexRep.getID())) {
						tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
						gl.glLineWidth(3);
						gl.glColor4fv(tmpNodeColor, 0);
						gl.glBegin(GL2.GL_LINE_STRIP);
						for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
							gl.glVertex3f(coords.get(pointIndex).getFirst()
									* PathwayRenderStyle.SCALING_FACTOR_X,
									-coords.get(pointIndex).getSecond()
											* PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
						}
						gl.glEnd();
					}
				}
			}
		}
		else {
			// Handle selection highlighting of element
			if (internalSelectionManager.checkStatus(SelectionType.SELECTION,
					vertexRep.getID())) {
				tmpNodeColor = SelectionType.SELECTION.getColor();
			}
			else if (internalSelectionManager.checkStatus(SelectionType.MOUSE_OVER,
					vertexRep.getID())) {
				tmpNodeColor = SelectionType.MOUSE_OVER.getColor();
			}
			// else if (internalSelectionManager.checkStatus(
			// SelectionType.NORMAL, vertexRep.getID())) {
			// tmpNodeColor = PathwayRenderStyle.ENZYME_NODE_COLOR;
			// }
			else {
				tmpNodeColor = PathwayRenderStyle.ENZYME_NODE_COLOR;
				// tmpNodeColor = new float[] { 0, 0, 0, 0 };
			}

			gl.glColor4fv(tmpNodeColor, 0);
			gl.glLineWidth(3);
			gl.glBegin(GL2.GL_LINE_STRIP);
			for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
				gl.glVertex3f(coords.get(pointIndex).getFirst()
						* PathwayRenderStyle.SCALING_FACTOR_X, -coords.get(pointIndex)
						.getSecond() * PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
			}
			gl.glEnd();

			if (!internalSelectionManager.checkStatus(SelectionType.DESELECTED,
					vertexRep.getID())) {

				// Transparent node for picking
				gl.glColor4f(0, 0, 0, 0);
				gl.glBegin(GL2.GL_POLYGON);
				for (int pointIndex = 0; pointIndex < coords.size(); pointIndex++) {
					gl.glVertex3f(coords.get(pointIndex).getFirst()
							* PathwayRenderStyle.SCALING_FACTOR_X, -coords.get(pointIndex)
							.getSecond() * PathwayRenderStyle.SCALING_FACTOR_Y, Z_OFFSET);
				}
				gl.glEnd();
			}
		}

	}

	private void createEdge(final GL2 gl, PathwayRelationEdgeRep edgeRep,
			PathwayGraph containingPathway) {

		// List<IGraphItem> listGraphItemsIn = edgeRep
		// .getAllItemsByProp(EGraphItemProperty.INCOMING);
		// List<IGraphItem> listGraphItemsOut = edgeRep
		// .getAllItemsByProp(EGraphItemProperty.OUTGOING);
		//
		// if (listGraphItemsIn.isEmpty() || listGraphItemsOut.isEmpty())
		// return;
		//
		// float[] tmpColor;
		// float fReactionLineOffset = 0;
		//
		// // Check if edge is a reaction
		// if (edgeRep instanceof PathwayReactionEdgeGraphItemRep) {
		// tmpColor = PathwayRenderStyle.REACTION_EDGE_COLOR;
		// fReactionLineOffset = 0.01f;
		// }
		// // Check if edge is a relation
		// else if (edgeRep instanceof PathwayRelationEdgeGraphItemRep) {
		// tmpColor = PathwayRenderStyle.RELATION_EDGE_COLOR;
		// } else {
		// tmpColor = new float[] { 0, 0, 0, 0 };
		// }
		//
		// gl.glLineWidth(4);
		// gl.glColor4fv(tmpColor, 0);
		// gl.glBegin(GL2.GL_LINES);
		//
		// Iterator<IGraphItem> iterSourceGraphItem =
		// listGraphItemsIn.iterator();
		// Iterator<IGraphItem> iterTargetGraphItem =
		// listGraphItemsOut.iterator();
		//
		// PathwayVertexGraphItemRep tmpSourceGraphItem;
		// PathwayVertexGraphItemRep tmpTargetGraphItem;
		// while (iterSourceGraphItem.hasNext()) {
		//
		// tmpSourceGraphItem = (PathwayVertexGraphItemRep)
		// iterSourceGraphItem.next();
		//
		// while (iterTargetGraphItem.hasNext()) {
		// tmpTargetGraphItem = (PathwayVertexGraphItemRep) iterTargetGraphItem
		// .next();
		//
		// gl.glVertex3f(tmpSourceGraphItem.getXOrigin()
		// * PathwayRenderStyle.SCALING_FACTOR_X + fReactionLineOffset,
		// -tmpSourceGraphItem.getYOrigin()
		// * PathwayRenderStyle.SCALING_FACTOR_Y
		// + fReactionLineOffset, 0.02f);
		// gl.glVertex3f(tmpTargetGraphItem.getXOrigin()
		// * PathwayRenderStyle.SCALING_FACTOR_X + fReactionLineOffset,
		// -tmpTargetGraphItem.getYOrigin()
		// * PathwayRenderStyle.SCALING_FACTOR_Y
		// + fReactionLineOffset, 0.02f);
		// }
		// }
		//
		// gl.glEnd();
	}

	public void renderPathway(final GL2 gl, final PathwayGraph pathway, boolean bRenderLabels) {
		if (enableEdgeRendering || !selectedEdgeRepId.isEmpty()) {
			int tmpEdgesDisplayListID = hashPathway2EdgesDisplayListId.get(pathway);
			gl.glCallList(tmpEdgesDisplayListID);
		}

		Integer tmpVerticesDisplayListID = hashPathway2VerticesDisplayListId.get(pathway);

		if (tmpVerticesDisplayListID != null) {
			gl.glCallList(tmpVerticesDisplayListID);

			// if (bRenderLabels && bEnableAnnotation)
			// renderLabels(gl, iPathwayID);
		}
	}

	private float[] determineNodeColor(PathwayVertexRep vertexRep) {

		int davidID = pathwayItemManager.getDavidIdByPathwayVertex((PathwayVertex) vertexRep
				.getPathwayVertices().get(0));

		if (davidID == -1 || davidID == 0)
			return null;
		else {

			Set<Integer> expressionIndices = idMappingManager.<Integer, Integer> getIDAsSet(
					glPathwayView.getPathwayDataDomain().getDavidIDType(), glPathwayView
							.getGeneSelectionManager().getIDType(), davidID);
			if (expressionIndices == null)
				return null;
			for (Integer expressionIndex : expressionIndices) {

				float expression = 0;

				if (glPathwayView.getGeneSelectionManager().getIDType() == geneticDataDomain
						.getRecordIDType())
					expression = (float) glPathwayView.getDataContainer()
							.getContainerStatistics().getAverageRecords().get(expressionIndex)
							.getArithmeticMean();
				else {

					int index = glPathwayView.getDataContainer().getDimensionPerspective()
							.getVirtualArray().indexOf(expressionIndex);
					if (index > 0)
						expression = (float) glPathwayView.getDataContainer()
								.getContainerStatistics().getAverageDimensions().get(index)
								.getArithmeticMean();
				}

				return colorMapper.getColor(expression);
			}
		}

		return null;
	}

	public void enableEdgeRendering(final boolean bEnableEdgeRendering) {
		this.enableEdgeRendering = bEnableEdgeRendering;
	}

	public void enableGeneMapping(final boolean bEnableGeneMappging) {
		this.enableGeneMapping = bEnableGeneMappging;
	}

	public void enableNeighborhood(final boolean bEnableNeighborhood) {
	}

	public void switchDataRepresentation() {
		if (dimensionDataRepresentation.equals(DataRepresentation.NORMALIZED)) {
			if (!geneticDataDomain.getTable().containsFoldChangeRepresentation())
				geneticDataDomain.getTable().createFoldChangeRepresentation();
			dimensionDataRepresentation = DataRepresentation.FOLD_CHANGE_NORMALIZED;
		}
		else
			dimensionDataRepresentation = DataRepresentation.NORMALIZED;
	}
}
