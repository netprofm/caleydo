package org.caleydo.view.scatterplot.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.storagebased.SetPointSizeEvent;
import org.caleydo.view.scatterplot.GLScatterplot;

/**
 * Listener that reacts events for setting the max. displayed hierarchy depth in RadialHierarchy.
 * 
 * @author Juergen Pillhofer
 */

public class SetPointSizeListener
	extends AEventListener<GLScatterplot> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof SetPointSizeEvent) {
			handler.setPointSize(((SetPointSizeEvent) event).getPointSize());

		}
	}
}
