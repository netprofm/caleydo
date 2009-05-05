package org.caleydo.core.manager.event.view.storagebased;

import org.caleydo.core.data.selection.delta.IVirtualArrayDelta;
import org.caleydo.core.manager.event.AEvent;

/**
 * Event to signal that ???
 * FIXME description about the meaning of virtual array deltas
 * Migration from EEventType.VA_UPDATE 
 * @author Werner Puff
 */
public class VirtualArrayUpdateEvent
	extends AEvent {
	
	/** delta between old and new selection */
	private IVirtualArrayDelta virtualArrayDelta;

	/**	additional information about the selection, e.g. to display in the info-box */
	private String info;

	public IVirtualArrayDelta getVirtualArrayDelta() {
		return virtualArrayDelta;
	}

	public void setVirtualArrayDelta(IVirtualArrayDelta virtualArrayDelta) {
		this.virtualArrayDelta = virtualArrayDelta;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
