package org.caleydo.core.event.view.grouper;

import org.caleydo.core.event.AEvent;

public class PasteGroupsEvent
	extends AEvent {

	private int iParentGroupID = -1;

	public PasteGroupsEvent(int iParentGroupID) {
		this.iParentGroupID = iParentGroupID;
	}

	@Override
	public boolean checkIntegrity() {
		return iParentGroupID != -1;
	}

	public int getParentGroupID() {
		return iParentGroupID;
	}

	public void setParentGroupID(int iParentGroupID) {
		this.iParentGroupID = iParentGroupID;
	}

}