package org.caleydo.core.gui.toolbar.listener;

import org.caleydo.core.gui.toolbar.ToolBarMediator;
import org.caleydo.core.manager.event.AEvent;

/**
 * Listens to all types of events that trigger a highlighting of a view specific toolbar group and invokes the
 * handling method on the related {@link ToolBarMediator}.
 * 
 * @author Werner Puff
 */
public class GroupHighlightingListener
	extends ToolBarListener {

	@Override
	public void handleEvent(AEvent event) {
		handler.highlightViewSpecificGroup(event.getSender());
	}

}