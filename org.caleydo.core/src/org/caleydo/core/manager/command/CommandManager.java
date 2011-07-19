package org.caleydo.core.manager.command;

import org.caleydo.core.command.CmdCreateIDCategory;
import org.caleydo.core.command.CmdCreateIDType;
import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.ICommand;
import org.caleydo.core.command.data.CmdDataCreateDataDomain;
import org.caleydo.core.command.data.CmdDataCreateSet;
import org.caleydo.core.command.data.CmdDataCreateStorage;
import org.caleydo.core.command.data.parser.CmdLoadFileLookupTable;
import org.caleydo.core.command.data.parser.CmdLoadFileNStorages;
import org.caleydo.core.command.view.CmdViewCreateRcpGLCanvas;
import org.caleydo.core.parser.parameter.ParameterHandler;

/**
 * Manager for creating commands.
 * 
 * @author Marc Streit
 */
public class CommandManager {

	/**
	 * Create a new command. Calls createCommandByType(CommandType) internal.
	 * 
	 * @param phAttributes
	 *            Define several attributes and assign them in new Command
	 * @return new Command with attributes defined in phAttributes
	 */
	public ICommand createCommand(final ParameterHandler phAttributes) {

		CommandType cmdType =
			CommandType.valueOf(phAttributes.getValueString(CommandType.TAG_TYPE.getXmlKey()));

		ICommand createdCommand = createCommandByType(cmdType);

		if (phAttributes != null) {
			createdCommand.setParameterHandler(phAttributes);
		}

		createdCommand.doCommand();

		return createdCommand;
	}

	/**
	 * Create a new command assigned to a cmdType.
	 * 
	 * @param cmdType
	 *            specify the ICommand to be created.
	 * @return new ICommand
	 */
	public ICommand createCommandByType(final CommandType cmdType) {
		ICommand createdCommand = null;

		switch (cmdType) {

			case CREATE_ID_CATEGORY: {
				createdCommand = new CmdCreateIDCategory();
				break;
			}
			case CREATE_ID_TYPE: {
				createdCommand = new CmdCreateIDType();
				break;
			}
			case LOAD_LOOKUP_TABLE_FILE: {
				createdCommand = new CmdLoadFileLookupTable();
				break;
			}
			case LOAD_DATA_FILE: {
				createdCommand = new CmdLoadFileNStorages();
				break;
			}
			case CREATE_DATA_DOMAIN: {
				createdCommand = new CmdDataCreateDataDomain();
				break;
			}
			case CREATE_STORAGE: {
				createdCommand = new CmdDataCreateStorage();
				break;
			}
			case CREATE_SET_DATA: {
				createdCommand = new CmdDataCreateSet();
				break;
			}
			case CREATE_VIEW_RCP_GLCANVAS: {
				createdCommand = new CmdViewCreateRcpGLCanvas();
				break;
			}
			default:
				throw new IllegalStateException("Unsupported CommandQueue key= [" + cmdType + "]");
		}

		return createdCommand;
	}
}
