/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.manager.memento;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;

import cerberus.manager.GeneralManager;
import cerberus.manager.MementoManager;
import cerberus.manager.base.AbstractManagerImpl;
import cerberus.manager.data.CollectionManager;
import cerberus.manager.type.ManagerType;
import cerberus.manager.type.ManagerObjectType;
import cerberus.manager.singelton.SingeltonManager;
//import java.util.Enumeration;

import cerberus.command.memento.IMemento;
//import prometheus.data.collection.Set;
import cerberus.util.exception.CerberusExceptionType;
import cerberus.util.exception.CerberusRuntimeException;

/**
 * Simple IMemento Manager, that stores all IMemento's in a Vector.
 * 
 * @author Michael Kalkusch
 *
 */
public class MementoManagerSimple 
 extends AbstractManagerImpl
 implements MementoManager {
	
	private int iCurrentUniqueMementoId;	
	
	private int iVecMementoStorageSize;
	
	protected Vector<IMemento> vecMemento;
	
	protected Hashtable<Integer,Integer> hashMementoId2Index;
	
	protected final int iInitSizeMementoVector = 40;
	
	protected GeneralManager refGeneralManager = null;
	
	/**
	 * Constructor. 
	 * 
	 * Allocates Vector and Hashtable.
	 */
	public MementoManagerSimple(final GeneralManager setGeneralManager) {
		
		super( setGeneralManager,
				GeneralManager.iUniqueId_TypeOffset_Memento );
		
		vecMemento = new Vector<IMemento>(iInitSizeMementoVector);
		hashMementoId2Index = new Hashtable<Integer,Integer>(iInitSizeMementoVector*2);
		iVecMementoStorageSize = 0;
		
		assert setGeneralManager !=null : "MementoSimpleManager init with null-pointer for SingeltonManager";
		refGeneralManager = setGeneralManager;
		
		iCurrentUniqueMementoId = 
			CollectionManager.calculateId( 
					GeneralManager.iUniqueId_TypeOffset_Memento, 
					refGeneralManager );
		
		refGeneralManager.getSingelton().setMementoManager( this );
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#setMemento(prometheus.command.memento.Memento)
	 */
	public final int pushMemento( final IMemento addMemento) {
		final int iUniqueId = createNewId( ManagerObjectType.MEMENTO );
		
		try {
			vecMemento.add(addMemento);
			iVecMementoStorageSize = vecMemento.size();			
			hashMementoId2Index.put( iUniqueId, iVecMementoStorageSize-1 );
			return iUniqueId;

		} catch (Exception e) {
			throw new CerberusRuntimeException("setMemento(IMemento) failed. " + e.toString(),
					CerberusExceptionType.MEMENTO );
		}
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#getMemento(int)
	 */
	public IMemento getMemento(int iMementoId) {
		
		final int iVectorIndex = hashMementoId2Index.get( iMementoId );
		
		try {
			return vecMemento.get(iVectorIndex);
		} catch (ArrayIndexOutOfBoundsException ae) {
			assert false:"getMemento(int) failed due to wrong iMementoId. " + ae.toString();
			return null;
		}
	}
	
	public IMemento pullMemento( final int iMementoId )
	{
		final int iIndex = hashMementoId2Index.get( iMementoId );
		
		try {
			IMemento pullMemento = vecMemento.get(iIndex);
			vecMemento.removeElementAt(iIndex);
			return pullMemento;
		} 
		catch (ArrayIndexOutOfBoundsException ae) {
			assert false:"getMemento(int) failed due to wrong iMementoId. " + ae.toString();
			return null;
		}
	}
	
	public boolean pullMemento( IMemento pullMemento )
	{
		return vecMemento.contains( pullMemento );
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#isMementoId(int)
	 */
	public boolean isMementoId(int iMementoId) {
		
		return this.hashMementoId2Index.contains( iMementoId );
	}
	

	/* (non-Javadoc)
	 * @see prometheus.data.manager.GeneralManagerInterface#size()
	 */
	public final int size() {		
		return vecMemento.size();
	}

	/* (non-Javadoc)
	 * @see prometheus.data.manager.GeneralManagerInterface#getManagerType()
	 */
	public final ManagerObjectType getManagerType() {		
		return ManagerObjectType.MEMENTO;
	}
	
	public String toString() {
		final String newLine = "\n";
		String result ="[";
		
		// Show content of Vector ...
		
		int iCounter = 0;
		Iterator<IMemento> iter  = vecMemento.iterator();		
		
		if ( ! iter.hasNext() ) {
			result += "Vector: is empty" + newLine;		
		}
		
		while ( iter.hasNext() ) {
			IMemento buffer = iter.next();
			
			if (buffer != null ) {
				result += " #" + iCounter + ": " + buffer.toString() + newLine;
			} else {
				result += " #" + iCounter + ": null" + newLine;
			}
			
			iCounter++;
		}
		
		result += "]"+newLine;
		
		return result;
	}
	
	/**
	 * Not used.
	 */
	public void optimize() {
		
	}

	 
	 /*
	  *  (non-Javadoc)
	  * @see prometheus.data.manager.MementoManager#clearAllMementos()
	  */
	 public void clearAllMementos() {
		 vecMemento.clear();
		 hashMementoId2Index.clear();
		 iVecMementoStorageSize = vecMemento.size();		 
	 }
	 
	
	/**
	 * Mementos are internal structures and can not be search for.
	 * 
	 * @see cerberus.manager.GeneralManager#hasItem(int)
	 */
	public final boolean hasItem(final int iItemId) {
		return false;
	}
	
	public final Object getItem( final int iItemId) {
		return null;
	}
	
	/**
	 * Writes all current stored mementos to the ObjectOutputStream.
	 * 
	 * @return true on success
	 */
	public boolean writeToOutputStream( ObjectOutputStream outStream) {
		
		//TODO: code this
		return true;
	}
	
	/**
	 *  Reads stored mementos from ObjectInputStream.
	 * 
	 * @return true on success
	 */
	public boolean readFromInputStream( ObjectInputStream inStream ) {
		
		//TODO: code this
		
		return true;
	}
	
	public boolean unregisterItem( final int iItemId,
			final ManagerObjectType type  ) {
		
		assert false:"not done yet";
		return false;
	}

	public boolean registerItem( final Object registerItem, 
			final int iItemId , 
			final ManagerObjectType type ) {
		
		
		assert false:"not done yet";
		return false;
	}

}
