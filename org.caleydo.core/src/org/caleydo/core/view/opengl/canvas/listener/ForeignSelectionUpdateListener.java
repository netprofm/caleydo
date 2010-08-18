package org.caleydo.core.view.opengl.canvas.listener;

import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.manager.datadomain.ISetBasedDataDomain;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.view.opengl.util.vislink.VisLinkScene;

/**
 * Listener for selection update events. This listener gets the payload from a SelectionUpdateEvent and calls
 * a related {@link ISelectionUpdateHandler}.
 * 
 * @author Werner Puff
 */
public class ForeignSelectionUpdateListener
	extends AEventListener<ISetBasedDataDomain> {

	/**
	 * Handles {@link SelectionUdpateEvent}s by extracting the event's payload and calling the related handler
	 * 
	 * @param event
	 *            {@link SelectionUpdateEvent} to handle, other events will be ignored
	 */
	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof SelectionUpdateEvent) {
			SelectionUpdateEvent selectioUpdateEvent = (SelectionUpdateEvent) event;
			ISelectionDelta delta = selectioUpdateEvent.getSelectionDelta();
			boolean scrollToSelection = selectioUpdateEvent.isScrollToSelection();
			String info = selectioUpdateEvent.getInfo();
			handler.handleForeignSelectionUpdate(selectioUpdateEvent.getDataDomainType(), delta, scrollToSelection, info);
			VisLinkScene.resetAnimation(System.currentTimeMillis());
		}
	}

}
