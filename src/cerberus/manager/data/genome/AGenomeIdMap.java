/**
 * 
 */
package cerberus.manager.data.genome;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import cerberus.data.mapping.GenomeMappingDataType;


/**
 * @author Michael Kalkusch
 *
 */
public abstract class AGenomeIdMap <K,V> 
implements IGenomeIdMap {

	protected HashMap <K,V> hashGeneric;
	
	protected final GenomeMappingDataType dataType;
	
	/**
	 * 
	 */
	public AGenomeIdMap(final GenomeMappingDataType dataType) {
		hashGeneric = new HashMap <K,V> ();
		this.dataType = dataType;
	}
	
	/**
	 * 
	 * @param iSizeHashMap define size of hashmap
	 */
	protected AGenomeIdMap(final GenomeMappingDataType dataType, final int iSizeHashMap) {
		hashGeneric = new HashMap <K,V> (iSizeHashMap);
		this.dataType = dataType;
	}

	/* (non-Javadoc)
	 * @see cerberus.manager.event.IEventPublisherMap#getIntByInt(int)
	 */
	public int getIntByInt(int key) {

		assert false : "getIntByInt() is not overloaded and thus can not be used!";
		return 0;
	}

	/* (non-Javadoc)
	 * @see cerberus.manager.event.IEventPublisherMap#getIntByString(java.lang.String)
	 */
	public int getIntByString(String key) {

		assert false : "getIntByInt() is not overloaded and thus can not be used!";
		return 0;
	}

	/* (non-Javadoc)
	 * @see cerberus.manager.event.IEventPublisherMap#getStringByInt(int)
	 */
	public String getStringByInt(int key) {

		assert false : "getIntByInt() is not overloaded and thus can not be used!";
		return "";
	}

	/* (non-Javadoc)
	 * @see cerberus.manager.event.IEventPublisherMap#getStringByString(java.lang.String)
	 */
	public String getStringByString(String key) {

		assert false : "getIntByInt() is not overloaded and thus can not be used!";
		return "";
	}

	/**
	 * @see cerberus.manager.data.genome.IGenomeIdMap#size()
	 * @see java.util.Map#size()
	 */
	public final int size() {
		return hashGeneric.size();
	}
	
	/**
	 * @see cerberus.manager.data.genome.IGenomeIdMap#getReversedMap()
	 */
	public final IGenomeIdMap getReversedMap() {
		IGenomeIdMap reversedMap = null;
		
			switch ( dataType ) 
			{
			case INT2INT:
				reversedMap = new GenomeIdMapInt2Int(dataType,
						this.size());
				break;
				
			case STRING2STRING:
				reversedMap = new GenomeIdMapString2String(dataType,
						this.size());
				break;
				
				/* invert type for reverse map! */
			case INT2STRING:
				/* ==> use STRING2INT */
				reversedMap = new GenomeIdMapString2Int(
						GenomeMappingDataType.STRING2INT,
						this.size());
				break;
				
			case STRING2INT:
				/* ==> use INT2STRING */
				reversedMap = new GenomeIdMapInt2String(
						GenomeMappingDataType.INT2STRING,
						this.size());
				break;
				
				default:
					assert false : "unsupported data type=" + dataType.toString();
			}	
	
		/** 
		 * Read HashMap and write it to new HashMap
		 */
		Set <Entry<K,V>> entrySet = hashGeneric.entrySet();			
		Iterator <Entry<K,V>> iterOrigin = entrySet.iterator();
		
		while ( iterOrigin.hasNext() ) 
		{
			Entry<K,V> EntryBuffer = iterOrigin.next();
			
			reversedMap.put( 
					EntryBuffer.getValue().toString() , 
					EntryBuffer.getKey().toString() );
		}
			
		return reversedMap;
	}
		
}
