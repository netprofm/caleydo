package cerberus.command.base;



import cerberus.command.CommandQueueSaxType;
import cerberus.command.ICommand;
import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.xml.parser.parameter.IParameterHandler;

/**
 * Abstract command class stores and handles commandId, tragertId and label of object.
 * @author Michael Kalkusch
 *
 */
public abstract class ACmdCreate_IdTargetLabel 
extends ACommand
implements ICommand
{
	/**
	 * Command Id to identify this command.
	 * 
	 * @deprecated remove this
	 */
	protected int iCommandId;
	
	/**
	 * Unique Id of the object, that will be created.
	 */
	protected int iUniqueTargetId;
	
	/**
	 * Label of the new object, that will be created.
	 */
	protected String sLabel = "";

	
	/**
	 * Constructor.
	 * 
	 * @param refGeneralManager
	 */
	protected ACmdCreate_IdTargetLabel(final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager)
	{
		// set unique ID to -1 because it is unknown at this moment
		super(-1, 
				refGeneralManager,
				refCommandManager);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param refGeneralManager
	 * 
	 * @deprecated
	 */
	protected ACmdCreate_IdTargetLabel(final IGeneralManager refGeneralManager)
	{
		// set unique ID to -1 because it is unknown at this moment
		super(-1, 
				refGeneralManager,
				refGeneralManager.getSingelton().getCommandManager());
	}
	
	
	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
		
		/*
		 * do not call empty method super.setParameterHandler()
		 */
		
		this.setId( 
				refParameterHandler.getValueInt( 
						CommandQueueSaxType.TAG_CMD_ID.getXmlKey() ) );
	
		iUniqueTargetId = 
			refParameterHandler.getValueInt( 
					CommandQueueSaxType.TAG_TARGET_ID.getXmlKey() );
		
		sLabel = refParameterHandler.getValueString( 
					CommandQueueSaxType.TAG_LABEL.getXmlKey() );
	}
	
}
