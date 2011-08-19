package org.caleydo.core.data.virtualarray.events;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.virtualarray.delta.VirtualArrayDelta;
import org.caleydo.core.manager.event.AEvent;

/**
 * Event to signal that the virtual array has changed. It carries a {@link VirtualArrayDelta} as payload which
 * adapts the recipients virtual array for example by removing items.
 * 
 * @author Alexander Lex
 */
@XmlRootElement
@XmlType
public class RecordVAUpdateEvent
	extends AEvent {

	/** additional information about the selection, e.g. to display in the info-box */
	private String info;

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}