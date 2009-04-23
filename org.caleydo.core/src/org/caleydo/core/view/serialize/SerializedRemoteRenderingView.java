package org.caleydo.core.view.serialize;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of the remote-rendering view (bucket). 
 * @author Werner Puff
 */
public class SerializedRemoteRenderingView 
	extends ASerializedView {

	private boolean pathwayTexturesEnabled;
	
	private boolean geneMappingEnabled;
	
	private boolean neighborhoodEnabled;
	
	@Override
	public ECommandType getCreationCommandType() {
		return ECommandType.CREATE_GL_BUCKET_3D;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		return null;
	}

	public boolean isPathwayTexturesEnabled() {
		return pathwayTexturesEnabled;
	}

	public void setPathwayTexturesEnabled(boolean pathwayTexturesEnabled) {
		this.pathwayTexturesEnabled = pathwayTexturesEnabled;
	}

	public boolean isGeneMappingEnabled() {
		return geneMappingEnabled;
	}

	public void setGeneMappingEnabled(boolean geneMappingEnabled) {
		this.geneMappingEnabled = geneMappingEnabled;
	}

	public boolean isNeighborhoodEnabled() {
		return neighborhoodEnabled;
	}

	public void setNeighborhoodEnabled(boolean neighborhoodEnabled) {
		this.neighborhoodEnabled = neighborhoodEnabled;
	}

}
