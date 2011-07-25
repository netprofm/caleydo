package org.caleydo.view.parcoords.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.tablebased.UseRandomSamplingEvent;
import org.caleydo.view.parcoords.GLParallelCoordinates;

public class UseRandomSamplingListener extends AEventListener<GLParallelCoordinates> {

	@Override
	public void handleEvent(AEvent event) {

		if (event instanceof UseRandomSamplingEvent) {
			UseRandomSamplingEvent randomSamplingEvent = (UseRandomSamplingEvent) event;
			handler.useRandomSampling(randomSamplingEvent.getFlag());
		}

	}

}
