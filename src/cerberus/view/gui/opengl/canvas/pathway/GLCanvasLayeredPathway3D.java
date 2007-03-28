package cerberus.view.gui.opengl.canvas.pathway;

import java.util.Iterator;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

import cerberus.data.collection.IStorage;
import cerberus.data.collection.StorageType;
import cerberus.data.pathway.Pathway;
import cerberus.data.pathway.element.PathwayVertex;
import cerberus.data.view.rep.pathway.IPathwayVertexRep;
import cerberus.manager.IGeneralManager;

/**
 * @author Marc Streit
 *
 */
public class GLCanvasLayeredPathway3D
extends AGLCanvasPathway3D {
	
	/**
	 * Constructor
	 * 
	 */
	public GLCanvasLayeredPathway3D( 
			final IGeneralManager refGeneralManager,
			int iViewId, 
			int iParentContainerId, 
			String sLabel ) {
				
		super(refGeneralManager, iViewId, iParentContainerId, sLabel);
	}	

	protected void buildPathwayDisplayList(final GL gl) {

		Pathway refTmpPathway = null;
		fZLayerValue = 0.0f;
		
		//refHashDisplayListNodeId2Pathway.clear();
		//refHashPathway2DisplayListNodeId.clear();
		//refHashPathwayToZLayerValue.clear();
		//iArPathwayNodeDisplayListIDs.clear();
		//iArPathwayEdgeDisplayListIDs.clear();
		
		buildEnzymeNodeDisplayList(gl);
		buildHighlightedEnzymeNodeDisplayList(gl);
		buildCompoundNodeDisplayList(gl);
		buildHighlightedCompoundNodeDisplayList(gl);
		
		System.out.println("Create pathway display lists");

		// Load pathway storage
		// Assumes that the set consists of only one storage
		IStorage tmpStorage = alSetData.get(0).getStorageByDimAndIndex(0, 0);
		int[] iArPathwayIDs = tmpStorage.getArrayInt();
		
		for (int iPathwayIndex = 0; iPathwayIndex < tmpStorage.getSize(StorageType.INT); 
			iPathwayIndex++)
		{			
			refTmpPathway = (Pathway)refGeneralManager.getSingelton().getPathwayManager().
				getItem(iArPathwayIDs[iPathwayIndex]);
			
			System.out.println("Create display list for pathway " +refTmpPathway.getTitle());
			
			refHashPathwayToZLayerValue.put(refTmpPathway, fZLayerValue);

			createPathwayDisplayList(gl, refTmpPathway);

			fZLayerValue += 1.5f;
		}
	}
		
	public void renderPart(GL gl) {
	
		handlePicking(gl);
				
		//FIXME:
		//fullscreen seetings
		renderInfoArea(gl, 0.0f, 0.2f, 0.0f);
		
		if (bSelectionDataChanged)
		{
			buildPathwayDisplayList(gl);
			bSelectionDataChanged = false;
		}
		
		int iDisplayListNodeId = 0;
		int iDisplayListEdgeId = 0;
		
		// Rebuild highlight node display list using the new scaling factor
		if (!iArHighlightedVertices.isEmpty())
		{
			if (bBlowUp == true)
			{
				fHighlightedNodeBlowFactor += 0.010f;
				
				if (fHighlightedNodeBlowFactor >= 1.3f)
					bBlowUp = false;
			}
			else
			{
				fHighlightedNodeBlowFactor -= 0.010;
				
				if (fHighlightedNodeBlowFactor <= 1.0f)
					bBlowUp = true;
			}
			
			buildHighlightedEnzymeNodeDisplayList(gl);
			buildHighlightedCompoundNodeDisplayList(gl);		
		}
		
		gl.glPushMatrix();
		
		Pathway refTmpPathway = null;
		
		// Load pathway storage
		// Assumes that the set consists of only one storage
		IStorage tmpStorage = alSetData.get(0).getStorageByDimAndIndex(0, 0);
		int[] iArPathwayIDs = tmpStorage.getArrayInt();
//		String sPathwayTexturePath = "";
//		int iPathwayId = 0;
		
		for (int iPathwayIndex = 0; iPathwayIndex < tmpStorage.getSize(StorageType.INT); 
			iPathwayIndex++)
		{
			refTmpPathway = (Pathway)refGeneralManager.getSingelton().getPathwayManager().
				getItem(iArPathwayIDs[iPathwayIndex]);
		
			iDisplayListNodeId = refHashPathway2DisplayListNodeId.get(refTmpPathway);
			//iDisplayListEdgeId = iArPathwayEdgeDisplayListIDs.get(iDisplayListIndex);
			
			//System.out.println("Accessing display list: " +iDisplayListNodeId + " " + iDisplayListEdgeId);
			
			//gl.glTranslatef(0.0f, 0.0f, 1.5f);
			
			if (bShowPathwayTexture == false)
			{
				gl.glCallList(iDisplayListEdgeId);
			}
//			// Creating hierarchical picking names
//			// This is the layer of the pathways, therefore we can use the pathway
//			// node picking ID
			gl.glPushName(iDisplayListNodeId);	
			gl.glCallList(iDisplayListNodeId);
			gl.glPopName();
		}
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		if (bShowPathwayTexture == true)
		{
			//gl.glDisable(GL.GL_LIGHTING);
			
			Iterator<Pathway> iterPathways = 
				refHashPathwayToTexture.keySet().iterator();
				
			//refHashPathway2ModelMatrix.clear();
			Texture refPathwayTexture = null;
			float fTmpZLayerValue = 0.0f;
			
			float fTextureWidth;
			float fTextureHeight;
			
			while (iterPathways.hasNext())
			{			
				//gl.glTranslatef(0.0f, 0.0f, 1.5f);
				
				refTmpPathway = iterPathways.next();
				refPathwayTexture = refHashPathwayToTexture.get(refTmpPathway);
				fTmpZLayerValue = refHashPathwayToZLayerValue.get(refTmpPathway);
				
				refPathwayTexture.enable();
				refPathwayTexture.bind();
				//gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
	
				gl.glColor4f(0.8f, 0.8f, 0.8f, 0.5f);
	
				TextureCoords texCoords = refPathwayTexture.getImageTexCoords();
				
				// Recalculate scaling factor
				fPathwayTextureAspectRatio = 
					(float)refPathwayTexture.getImageWidth() / 
					(float)refPathwayTexture.getImageHeight();								
				
				fTextureWidth = 0.0025f * (float)refPathwayTexture.getImageWidth();
				fTextureHeight = 0.0025f * (float)refPathwayTexture.getImageHeight();				
				
				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(0, texCoords.top()); 
				gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);			  
				gl.glTexCoord2f(texCoords.right(), texCoords.top()); 
				gl.glVertex3f(fTextureWidth, 0.0f, fTmpZLayerValue);			 
				gl.glTexCoord2f(texCoords.right(), 0); 
				gl.glVertex3f(fTextureWidth, fTextureHeight, fTmpZLayerValue);
				gl.glTexCoord2f(0, 0); 
				gl.glVertex3f(0.0f, fTextureHeight, fTmpZLayerValue);
				gl.glEnd();	
				
				gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
				gl.glLineWidth(1);
				gl.glBegin(GL.GL_LINE_STRIP); 
				gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);; 
				gl.glVertex3f(fTextureWidth, 0.0f, fTmpZLayerValue);
				gl.glVertex3f(fTextureWidth, fTextureHeight, fTmpZLayerValue);
				gl.glVertex3f(0.0f, fTextureHeight, fTmpZLayerValue);
				gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);; 				
				gl.glEnd();
				
//				gl.glTranslatef(texCoords.right() * 0.0025f, 0.0f, 0.0f);
//				
				refPathwayTexture.disable();
			}
			
			//gl.glEnable(GL.GL_LIGHTING);
		}
		gl.glPopMatrix();
		
		highlightIdenticalNodes(gl);
	}
	
	protected void renderPathway(final GL gl,
			final Pathway refTmpPathway, 
			int iDisplayListNodeId) {
		
		// Creating hierarchical picking names
		// This is the layer of the pathways, therefore we can use the pathway
		// node picking ID
		gl.glPushName(iDisplayListNodeId);	
		gl.glCallList(iDisplayListNodeId);
		gl.glPopName();
		
		Texture refPathwayTexture = null;
		float fTmpZLayerValue = 0.0f;
		float fTextureWidth;
		float fTextureHeight;
		
		refPathwayTexture = refHashPathwayToTexture.get(refTmpPathway);
		fTmpZLayerValue = refHashPathwayToZLayerValue.get(refTmpPathway);
		System.out.println("z-Layer value: " +fTmpZLayerValue);
		
		refPathwayTexture.enable();
		refPathwayTexture.bind();
		gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

		gl.glColor4f(0.8f, 0.8f, 0.8f, 0.5f);

		TextureCoords texCoords = refPathwayTexture.getImageTexCoords();
		
		// Recalculate scaling factor
		fPathwayTextureAspectRatio = 
			(float)refPathwayTexture.getImageWidth() / 
			(float)refPathwayTexture.getImageHeight();								
		
		fTextureWidth = 0.0025f * (float)refPathwayTexture.getImageWidth();
		fTextureHeight = 0.0025f * (float)refPathwayTexture.getImageHeight();				
		
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0, texCoords.top()); 
		gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);			  
		gl.glTexCoord2f(texCoords.right(), texCoords.top()); 
		gl.glVertex3f(fTextureWidth, 0.0f, fTmpZLayerValue);			 
		gl.glTexCoord2f(texCoords.right(), 0); 
		gl.glVertex3f(fTextureWidth, fTextureHeight, fTmpZLayerValue);
		gl.glTexCoord2f(0, 0); 
		gl.glVertex3f(0.0f, fTextureHeight, fTmpZLayerValue);
		gl.glEnd();	
		
		gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINE_STRIP); 
		gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);; 
		gl.glVertex3f(fTextureWidth, 0.0f, fTmpZLayerValue);
		gl.glVertex3f(fTextureWidth, fTextureHeight, fTmpZLayerValue);
		gl.glVertex3f(0.0f, fTextureHeight, fTmpZLayerValue);
		gl.glVertex3f(0.0f, 0.0f, fTmpZLayerValue);; 				
		gl.glEnd();

		refPathwayTexture.disable();
	}

	protected void connectVertices(final GL gl,
			IPathwayVertexRep refVertexRep1, 
			IPathwayVertexRep refVertexRep2) {

		float fZLayerValue1 = 0.0f; 
		float fZLayerValue2 = 0.0f;
		Pathway refTmpPathway = null;
		Iterator<Pathway> iterDrawnPathways = null;
		Texture refPathwayTexture = null;
		float fCanvasXPos1 = 0.0f;
		float fCanvasYPos1 = 0.0f;
		float fCanvasXPos2 = 0.0f;
		float fCanvasYPos2 = 0.0f;
		
		iterDrawnPathways = refHashPathwayToZLayerValue.keySet().iterator();
		
		while(iterDrawnPathways.hasNext())
		{
			refTmpPathway = iterDrawnPathways.next();

			// Recalculate scaling factor
			refPathwayTexture = refHashPathwayToTexture.get(refTmpPathway);
			fPathwayTextureAspectRatio = 
				(float)refPathwayTexture.getImageWidth() / 
				(float)refPathwayTexture.getImageHeight();								
			
			if(refTmpPathway.isVertexInPathway(refVertexRep1.getVertex()) == true)
			{					
				fZLayerValue1 = refHashPathwayToZLayerValue.get(refTmpPathway);
				
				fCanvasXPos1 = viewingFrame[X][MIN] + 
					refVertexRep1.getXPosition() * SCALING_FACTOR_X;
				fCanvasYPos1 = viewingFrame[Y][MIN] + 
					refVertexRep1.getYPosition() * SCALING_FACTOR_Y;
			}
			
			if(refTmpPathway.isVertexInPathway(refVertexRep2.getVertex()) == true)
			{					
				fZLayerValue2 = refHashPathwayToZLayerValue.get(refTmpPathway);
				
				fCanvasXPos2 = viewingFrame[X][MIN] + 
					refVertexRep2.getXPosition() * SCALING_FACTOR_X;
				fCanvasYPos2 = viewingFrame[Y][MIN] + 
					refVertexRep2.getYPosition() * SCALING_FACTOR_Y;
			}
		}
		
		gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
		gl.glLineWidth(3);
		gl.glBegin(GL.GL_LINES);		
			gl.glVertex3f(fCanvasXPos1, fCanvasYPos1, fZLayerValue1); 
			gl.glVertex3f(fCanvasXPos2, fCanvasYPos2, fZLayerValue2);					
		gl.glEnd();
		gl.glLineWidth(1);

	}

    protected void highlightIdenticalNodes(final GL gl) {
    	
		Pathway refTmpPathway = null;
		PathwayVertex refCurrentVertex = null;
		PathwayVertex refTmpVertex = null;
		IPathwayVertexRep refCurrentVertexRep = null;
		Iterator<PathwayVertex> iterIdenticalVertices = null;
		Iterator<Pathway> iterDrawnPathways = 
			refHashPathwayToZLayerValue.keySet().iterator();
		
		for (int iHighlightedNodeIndex = 0; iHighlightedNodeIndex < iArHighlightedVertices.size(); 
			iHighlightedNodeIndex++)
		{
		
			refCurrentVertex = ((IPathwayVertexRep)iArHighlightedVertices.
					get(iHighlightedNodeIndex)).getVertex();
					
			while(iterDrawnPathways.hasNext())
			{
				refTmpPathway = iterDrawnPathways.next();
				
				// Hightlight all identical nodes
				iterIdenticalVertices = refGeneralManager.getSingelton().
						getPathwayElementManager().getPathwayVertexListByName(
								refCurrentVertex.getElementTitle()).iterator();
				
				while(iterIdenticalVertices.hasNext())
				{
					refTmpVertex = iterIdenticalVertices.next();
					
					if (refTmpPathway.isVertexInPathway(refTmpVertex) == true)
					{
						fZLayerValue = refHashPathwayToZLayerValue.get(refTmpPathway);
					
						refCurrentVertexRep = refTmpVertex.
							getVertexRepByIndex(iVertexRepIndex);
						
						if (refCurrentVertexRep != null)
						{
							iArHighlightedVertices.add(refCurrentVertexRep);
							// Highlighted nodes in foreign pathways should get 
							// neighbor distance of 0 (normal highlig color)
							iArSelectionStorageNeighborDistance.add(0);
							
							createVertex(gl, refCurrentVertexRep, refTmpPathway);
							
							if (!refTmpPathway.equals(refPathwayUnderInteraction))
							{
								connectVertices(gl, refCurrentVertexRep, 
										refCurrentVertex.getVertexRepByIndex(iVertexRepIndex));
							}
						}
					}
				}
			}
	    }
    }
}
