package org.caleydo.view.grouper.contextmenu;

import java.util.Set;

import org.caleydo.core.view.opengl.util.overlay.contextmenu.AContextMenuItem;
import org.caleydo.view.grouper.event.CreateGroupEvent;

public class CreateGroupItem extends AContextMenuItem {

	public CreateGroupItem(Set<Integer> setContainedGroups) {
		super();
		setText("Create Group");
		CreateGroupEvent event = new CreateGroupEvent(setContainedGroups);
		event.setSender(this);
		registerEvent(event);
	}
}
