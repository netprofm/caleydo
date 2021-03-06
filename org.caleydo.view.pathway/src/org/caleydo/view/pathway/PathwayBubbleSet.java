/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
/**
 *
 */
package org.caleydo.view.pathway;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorManager;
import org.caleydo.datadomain.pathway.graph.PathSegment;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.PathwayPath;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import setvis.BubbleSetGLRenderer;



public class PathwayBubbleSet
{
	////////////////////////////////////////////////
	// private section
	////////////////////////////////////////////////
	private BubbleSetGLRenderer renderer= new BubbleSetGLRenderer();
	private PathwayGraph pathway=null;
	////////////////////////////////////////////////
	// public section
	////////////////////////////////////////////////
	public PathwayBubbleSet(){

	}
	public BubbleSetGLRenderer getBubbleSetGLRenderer(){
		return renderer;
	}
	public void setBubbleSetGLRenderer(BubbleSetGLRenderer newRenderer){
		renderer=newRenderer;
	}

	public void setPathwayGraph(PathwayGraph aPathway){
		pathway=aPathway;
	}

	public void clear(){
		renderer.clearBubbleSet();
	}

	public void addAllPaths(List<GraphPath<PathwayVertexRep, DefaultEdge>> allPaths){
		for (GraphPath<PathwayVertexRep, DefaultEdge> path : allPaths) {
			if (path == null)
				break;
			addPath(path);
		}
	}

	public void addAllPaths(List<GraphPath<PathwayVertexRep, DefaultEdge>> allPaths, int selectionID){
		List<org.caleydo.core.util.color.Color> colorTable = (ColorManager.get()).getColorList("qualitativeColors");
		int id=0;
		int colorID=0;
		GraphPath<PathwayVertexRep, DefaultEdge> selPath=null;
		// float [] selColor=SelectionType.SELECTION.getColor();

		//add selection first
		for (GraphPath<PathwayVertexRep, DefaultEdge> path : allPaths) {
			if (path == null)
				break;
			if(selectionID==id){
				//addColoredPath(path,new Color(selColor[0],selColor[1],selColor[2]));
				//addColoredPath(path,new Color(selColor[0],selColor[1],selColor[2]));
				//addColoredPath(path,new Color(selColor[0],selColor[1],selColor[2]));
				break;
			}
			id++;
		}
		id=0;
		for (GraphPath<PathwayVertexRep, DefaultEdge> path : allPaths) {

			if (id < colorTable.size() - 2)		{
				// avoid the last two colors because they are close to orange
				// which is the selection color
				colorID = id;
			}
			else
				colorID = colorTable.size() - 1;
			if (path == null)
				break;

			if(selectionID!=id){
				org.caleydo.core.util.color.Color c = colorTable.get(colorID);
				addColoredPath(path,new Color(c.r/1.0f,c.g/1.0f,c.b/1.0f));
			}
			id++;
		}
	}

	public void addColoredPath(GraphPath<PathwayVertexRep, DefaultEdge> path, Color colorValue)
	{
		// single node path = no edge exist
		if (path.getEndVertex() == path.getStartVertex()) {
			PathwayVertexRep sourceVertexRep = path.getEndVertex();
			double bbItemW = sourceVertexRep.getWidth();
			double bbItemH = sourceVertexRep.getHeight();
			double posX = sourceVertexRep.getLowerLeftCornerX();//getLowerLeftCornerX();
			double posY = sourceVertexRep.getLowerLeftCornerY();//.getLowerLeftCornerY();
			ArrayList<Rectangle2D> items= new ArrayList<>();
			items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
			renderer.addGroup(items, null, colorValue.getAWTColor());
		}
		// add path by adding each of its nodes
		else {
			if(pathway==null)return;
			ArrayList<Rectangle2D> items= new ArrayList<>();
			ArrayList<Line2D> edges= new ArrayList<>();
			for (DefaultEdge edge : path.getEdgeList()) {
				PathwayVertexRep sourceVertexRep = pathway.getEdgeSource(edge);
				PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(edge);

//				double bbItemW = sourceVertexRep.getWidth();
//				double bbItemH = sourceVertexRep.getHeight();
//				double posX = sourceVertexRep.getCenterX();
//				double posY = sourceVertexRep.getCenterY();
//				double tX = targetVertexRep.getCenterX();
//				double tY = targetVertexRep.getCenterY();
				double bbItemW = sourceVertexRep.getWidth();
				double bbItemH = sourceVertexRep.getHeight();
				double posX = sourceVertexRep.getLowerLeftCornerX();
				double posY = sourceVertexRep.getLowerLeftCornerY();
				double tX = targetVertexRep.getLowerLeftCornerX();
				double tY = targetVertexRep.getLowerLeftCornerY();

				items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
				edges.add(new Line2D.Double(posX, posY, tX, tY));
			}
			DefaultEdge lastEdge = path.getEdgeList().get(path.getEdgeList().size() - 1);
			if (lastEdge != null) {
				PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(lastEdge);
				items.add(new Rectangle2D.Double(
						targetVertexRep.getLowerLeftCornerX(), targetVertexRep.getLowerLeftCornerY(),
						targetVertexRep.getWidth(), targetVertexRep.getHeight()));
			}
			renderer.addGroup(items, edges, colorValue.getAWTColor());
		}
	}


	public void addPath(GraphPath<PathwayVertexRep, DefaultEdge> path)
	{
		// single node path = no edge exist
		if (path.getEndVertex() == path.getStartVertex()) {
			PathwayVertexRep sourceVertexRep = path.getEndVertex();
			double bbItemW = sourceVertexRep.getWidth();
			double bbItemH = sourceVertexRep.getHeight();
			double posX = sourceVertexRep.getLowerLeftCornerX();//getLowerLeftCornerX();
			double posY = sourceVertexRep.getLowerLeftCornerY();//.getLowerLeftCornerY();
			ArrayList<Rectangle2D> items= new ArrayList<>();
			items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
			renderer.addGroup(items, null , null);
		}
		// add path by adding each of its nodes
		else {
			if(pathway==null)return;
			ArrayList<Rectangle2D> items= new ArrayList<>();
			ArrayList<Line2D> edges= new ArrayList<>();
			for (DefaultEdge edge : path.getEdgeList()) {
				PathwayVertexRep sourceVertexRep = pathway.getEdgeSource(edge);
				PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(edge);

//				double bbItemW = sourceVertexRep.getWidth();
//				double bbItemH = sourceVertexRep.getHeight();
//				double posX = sourceVertexRep.getCenterX();
//				double posY = sourceVertexRep.getCenterY();
//				double tX = targetVertexRep.getCenterX();
//				double tY = targetVertexRep.getCenterY();
				double bbItemW = sourceVertexRep.getWidth();
				double bbItemH = sourceVertexRep.getHeight();
				double posX = sourceVertexRep.getLowerLeftCornerX();
				double posY = sourceVertexRep.getLowerLeftCornerY();
				double tX = targetVertexRep.getLowerLeftCornerX();
				double tY = targetVertexRep.getLowerLeftCornerY();

				items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
				edges.add(new Line2D.Double(posX, posY, tX, tY));
			}
			DefaultEdge lastEdge = path.getEdgeList().get(path.getEdgeList().size() - 1);
			if (lastEdge != null) {
				PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(lastEdge);
				items.add(new Rectangle2D.Double(
						targetVertexRep.getLowerLeftCornerX(), targetVertexRep.getLowerLeftCornerY(),
						targetVertexRep.getWidth(), targetVertexRep.getHeight()));
			}
			renderer.addGroup(items, edges , null);
		}
	}

	public void addContextPathSegements(List<List<PathwayVertexRep>> contextPaths){
		Color contextPathColor=new Color(0.0f,0.0f,1.0f);
		ArrayList<Rectangle2D> items= new ArrayList<>();
		ArrayList<Line2D> edges= new ArrayList<>();
		int i=0;
		for (List<PathwayVertexRep> pathSegment : contextPaths)
		{
			i=0;
			Rectangle2D prevRect = new Rectangle2D.Double(0f, 0f, 0f, 0f);
			edges.clear();
			items.clear();
			for (PathwayVertexRep node : pathSegment) {
				double bbItemW = node.getWidth();
				double bbItemH = node.getHeight();
				double posX = node.getLowerLeftCornerX();
				double posY = node.getLowerLeftCornerY();
				items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
				if (i > 0) {
					edges.add(new Line2D.Double(posX, posY, prevRect.getCenterX(), prevRect.getCenterY()));
				}
				prevRect.setRect(posX, posY, bbItemW, bbItemH);
				i++;
			}
			renderer.addGroup(items, edges, contextPathColor.getAWTColor());
		}
	}

	public void addPathSegements(PathwayPath pathSegments) {
		if (pathSegments.size() <= 0) return;

		Color pathSegColor = SelectionType.SELECTION.getColor();
		int outlineThickness = 3;
		for (PathSegment pathSegment : pathSegments)
		{
			if (pathSegment.getPathway() == pathway)
			{
				GraphPath<PathwayVertexRep, DefaultEdge> seg = pathSegment.asGraphPath();
				ArrayList<Rectangle2D> items= new ArrayList<>();
				ArrayList<Line2D> edges= new ArrayList<>();
				if (seg.getEdgeList().size() < 1) {
					PathwayVertexRep sourceVertexRep = seg.getStartVertex();
						double bbItemW = sourceVertexRep.getWidth();
						double bbItemH = sourceVertexRep.getHeight();
						double posX = sourceVertexRep.getLowerLeftCornerX();
						double posY = sourceVertexRep.getLowerLeftCornerY();
						items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
				 }else{
					for (DefaultEdge edge : seg.getEdgeList()) {
						PathwayVertexRep sourceVertexRep = pathway.getEdgeSource(edge);
						PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(edge);
						double bbItemW = sourceVertexRep.getWidth();
						double bbItemH = sourceVertexRep.getHeight();
						double posX = sourceVertexRep.getLowerLeftCornerX();
						double posY = sourceVertexRep.getLowerLeftCornerY();
						double tX = targetVertexRep.getLowerLeftCornerX();
						double tY = targetVertexRep.getLowerLeftCornerY();

						items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
						edges.add(new Line2D.Double(posX, posY, tX, tY));
					}
					// add last item
					if (seg.getEdgeList().size() > 0) {
						DefaultEdge lastEdge = seg.getEdgeList().get(seg.getEdgeList().size() - 1);
						if (lastEdge != null) {
							PathwayVertexRep targetVertexRep = pathway.getEdgeTarget(lastEdge);
							items.add(new Rectangle2D.Double(
									targetVertexRep.getLowerLeftCornerX(), targetVertexRep.getLowerLeftCornerY(),
									targetVertexRep.getWidth(), targetVertexRep.getHeight()));
						}
					}
				}
				renderer.addGroup(items, edges, pathSegColor.getAWTColor());
			}//if (pathSegment.getPathway() == pathway)
		}//for (PathwayPath pathSegment : pathSegments)
	}


	public void addPortals(Set<PathwayVertexRep> portalVertexReps){
		if(portalVertexReps==null)return;

		Color portalColor=new Color(1f,0f,0f);

		for (PathwayVertexRep portal : portalVertexReps) {
			double posX = portal.getLowerLeftCornerX();
			double posY = portal.getLowerLeftCornerY();
			double bbItemW = portal.getWidth();
			double bbItemH = portal.getHeight();
			ArrayList<Rectangle2D> items= new ArrayList<>();
			items.add(new Rectangle2D.Double(posX, posY, bbItemW, bbItemH));
			renderer.addGroup(items, null, portalColor.getAWTColor());
		}
	}

}

