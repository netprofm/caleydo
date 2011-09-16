package org.caleydo.core.view.opengl.canvas.listener;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.AEventListener;
import org.caleydo.core.event.view.group.InterchangeDimensionGroupsEvent;
import org.caleydo.core.view.opengl.canvas.remote.receiver.IDimensionGroupsActionHandler;

public class DimensionGroupInterChangingActionListener
	extends AEventListener<IDimensionGroupsActionHandler> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof InterchangeDimensionGroupsEvent) {

			handler.handleInterchangeDimensionGroups();
		}
	}
}
