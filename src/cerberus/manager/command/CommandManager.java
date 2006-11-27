/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.manager.command;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
//import java.util.Iterator;

import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.manager.base.AAbstractManager;
import cerberus.manager.command.factory.CommandFactory;
import cerberus.manager.command.factory.ICommandFactory;
import cerberus.manager.singelton.SingeltonManager;
import cerberus.manager.type.ManagerObjectType;
import cerberus.xml.parser.parameter.IParameterHandler;


import cerberus.command.ICommand;
import cerberus.command.ICommandListener;
import cerberus.command.CommandType;
import cerberus.command.queue.ICommandQueue;

/**
 * @author Michael Kalkusch
 *
 */
public class CommandManager 
 extends AAbstractManager 
 implements ICommandManager {

	private ICommandFactory refCommandFactory;
	
	/**
	 * List of all Commands to be excecuted as soon as possible
	 */
	private Vector<ICommand> vecCmd_handle;
	
	/**
	 * List of All Commands to be executed when sooner or later.
	 */
	private Vector<ICommand> vecCmd_schedule;
	
	protected Hashtable<Integer,ICommandQueue> hash_CommandQueueId;
	
	protected Hashtable<Integer,ICommand> hash_CommandId;
	
	/**
	 * 
	 */
	public CommandManager( IGeneralManager setGeneralManager ) {
		super( setGeneralManager, IGeneralManager.iUniqueId_TypeOffset_Command );
		
		refCommandFactory = new CommandFactory( setGeneralManager, 
				this, 
				null );
		
		vecCmd_handle = new Vector<ICommand> ();
		
		vecCmd_schedule = new Vector<ICommand> ();
		
		hash_CommandQueueId = new Hashtable<Integer,ICommandQueue> ();
		
		hash_CommandId = new Hashtable<Integer,ICommand> ();
				
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.CommandManager#addCommandListener(cerberus.command.ICommandListener)
	 */
	public void addCommandListener(ICommandListener addCommandListener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.CommandManager#removeCommandListener(cerberus.command.ICommandListener)
	 */
	public boolean removeCommandListener(ICommandListener removeCommandListener) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.CommandManager#hasCommandListener(cerberus.command.ICommandListener)
	 */
	public boolean hasCommandListener(ICommandListener hasCommandListener) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.command.ICommandActionListener#handleCommand(cerberus.command.ICommand)
	 */
	public void handleCommand(ICommand addCommand) {
		
		addCommand.doCommand();
		vecCmd_handle.addElement( addCommand );
	}

	/* (non-Javadoc)
	 * @see cerberus.command.ICommandActionListener#scheduleCommand(cerberus.command.ICommand)
	 */
	public void scheduleCommand(ICommand addCommand) {
		
		vecCmd_schedule.addElement( addCommand );
		addCommand.doCommand();
		
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#hasItem(int)
	 */
	public boolean hasItem(int iItemId) {
		return hash_CommandId.containsKey( iItemId );
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#getItem(int)
	 */
	public Object getItem(int iItemId) {
		return hash_CommandId.get( iItemId );
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#size()
	 */
	public int size() {
		return hash_CommandId.size();
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#getManagerType()
	 */
	public ManagerObjectType getManagerType() {
		return ManagerObjectType.COMMAND;
	}



	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#registerItem(java.lang.Object, int, cerberus.data.manager.BaseManagerType)
	 */
	public boolean registerItem(Object registerItem, int iItemId,
			ManagerObjectType type) {
		
		ICommand registerCommand = (ICommand) registerItem;
		
		if ( registerCommand.getClass().equals( 
				cerberus.command.queue.ICommandQueue.class )) {
			hash_CommandQueueId.put( iItemId, (ICommandQueue) registerItem );
		} else {
			
		}
		
		vecCmd_handle.addElement( registerCommand );		
		hash_CommandId.put( iItemId, registerCommand );
		
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#unregisterItem(int, cerberus.data.manager.BaseManagerType)
	 */
	public boolean unregisterItem(int iItemId, ManagerObjectType type) {	
		
		//TODO: ensure thread safety! 
		if ( type == ManagerObjectType.CMD_QUEUE ) {
			hash_CommandQueueId.remove( iItemId );
		}
		
		if ( hash_CommandId.containsKey( iItemId ) ) {
		
			ICommand unregisterCommand = 
				hash_CommandId.remove( iItemId );
			
			return vecCmd_handle.remove( unregisterCommand );
		}
		
		return false;
	}

	public ICommand createCommand( final CommandType useSelectionType, String details ) {
	
		return refCommandFactory.createCommand( useSelectionType, details );
	}
	
	
	
	public ICommand createCommand( final String useSelectionType ) {
		
		return refCommandFactory.createCommand( CommandType.getType( useSelectionType ), null );
	}
	
	
//	/**
//	 * Create a new command using the CommandType.
//	 * @param details TODO
//	 * 
//	 * @deprecated
//	 */
//	public ICommand createCommand( final String  useSelectionType, 
//			final LinkedList <String> llAttributes ) {
//		
//		ICommand createdCommand = 
//			refCommandFactory.createCommand( useSelectionType,
//					llAttributes );	
//		
//		if ( createdCommand.getId() < 0 ) {
//			createdCommand.setId( createNewId( ManagerObjectType.VIRTUAL_ARRAY_MULTI_BLOCK ) );
//		}
//		
//		registerItem( createdCommand, 
//				createdCommand.getId(),
//				ManagerObjectType.COMMAND );
//		
//		return createdCommand;
//	}
	
	public ICommand createCommand(final IParameterHandler phAttributes)
	{
		return refCommandFactory.createCommand( phAttributes );
	}
	
//	/* (non-Javadoc)
//	 * @see cerberus.data.manager.GeneralManager#createNewId(cerberus.data.manager.BaseManagerType)
//	 */
//	public int createNewId(ManagerObjectType setNewBaseType) {
//		
//		iUniqueId_current += iUniqueId_Increment;
//		
//		return iUniqueId_current;
//	}

	/*
	 * 
	 */
	public boolean hasCommandQueueId( final int iCmdQueueId ) {
		return hash_CommandQueueId.containsKey( iCmdQueueId );
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.manager.ICommandManager#getCommandQueueByCmdQueueId(int)
	 */
	public ICommandQueue getCommandQueueByCmdQueueId( final int iCmdQueueId ) {
		return hash_CommandQueueId.get( iCmdQueueId );
	}
	
	public ICommand createCommandQueue( final String sCmdType,
			final String sProcessType,
			final int iCmdId,
			final int iCmdQueueId,
			final int sQueueThread,
			final int sQueueThreadWait ) {
		
		ICommand newCmd = 
			refCommandFactory.createCommandQueue( sCmdType,
			sProcessType,
			iCmdId,
			iCmdQueueId,
			sQueueThread,
			sQueueThreadWait );
		
		int iNewCmdId = createNewId( ManagerObjectType.COMMAND );
		newCmd.setId( iNewCmdId );
		
		registerItem( newCmd, iNewCmdId, ManagerObjectType.COMMAND );
		
		return newCmd;
	}

	
}
