package org.caleydo.core.view.opengl.canvas.pathway.listeners;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.IEventListener;
import org.caleydo.core.view.opengl.canvas.pathway.GLPathway;

public class EnableNeighborhoodListener
	implements IEventListener {

	GLPathway glPathway = null;
	
	@Override
	public void handleEvent(AEvent event) {
		glPathway.enableNeighborhood(true);
	}

	public GLPathway getGLPathway() {
		return glPathway;
	}

	public void setGLPathway(GLPathway glPathway) {
		this.glPathway = glPathway;
	}

}
