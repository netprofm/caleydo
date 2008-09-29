package org.caleydo.core.manager.mapping;

import java.util.HashMap;
import java.util.logging.Level;
import org.caleydo.core.data.collection.EStorageType;
import org.caleydo.core.data.map.MultiHashMap;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.mapping.EMappingDataType;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.general.GeneralManager;

/**
 * Manages mapping tables.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public class IDMappingManager
	implements IIDMappingManager
{
	protected HashMap<EMappingType, HashMap<?, ?>> hashType2Mapping;

	private IGeneralManager generalManager = GeneralManager.get();

	/**
	 * Constructor.
	 * 
	 */
	public IDMappingManager()
	{
		hashType2Mapping = new HashMap<EMappingType, HashMap<?, ?>>();
	}

	@Override
	public void createMap(EMappingType type, EMappingDataType dataType)
	{
		generalManager.getLogger().log(Level.INFO,
				"Create lookup table for type=" + type);

		switch (dataType)
		{
			case INT2INT:
				hashType2Mapping.put(type, new HashMap<Integer, Integer>());
				break;
			case INT2STRING:
				hashType2Mapping.put(type, new HashMap<Integer, String>());
				break;
			case STRING2INT:
				hashType2Mapping.put(type, new HashMap<String, Integer>());
				break;
			case STRING2STRING:
				hashType2Mapping.put(type, new HashMap<String, String>());
				break;
			case MULTI_STRING2STRING:
				hashType2Mapping.put(type, new MultiHashMap<String, String>());
				break;
			case MULTI_INT2STRING:
				hashType2Mapping.put(type, new MultiHashMap<Integer, String>());
				break;
			case MULTI_STRING2INT:
				hashType2Mapping.put(type, new MultiHashMap<String, Integer>());
				break;		
			case MULTI_INT2INT:
				hashType2Mapping.put(type, new MultiHashMap<Integer, Integer>());
				break;				
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <SrcType, DestType> void createReverseMap(EMappingType srcType, EMappingType reverseType)
	{
		if (srcType.isMultiMap())
			hashType2Mapping.put(reverseType, new MultiHashMap<DestType, SrcType>());
		else
			hashType2Mapping.put(reverseType, new HashMap<DestType, SrcType>());
		
		HashMap<DestType, SrcType> reverseMap = (HashMap<DestType, SrcType>) hashType2Mapping.get(reverseType);
		HashMap<SrcType, DestType> sourceMap = (HashMap<SrcType, DestType>) hashType2Mapping.get(srcType);

		for (SrcType key : sourceMap.keySet())
		{
			reverseMap.put(sourceMap.get(key), key);
		}
	}
	
	/**
	 * Method takes a map that contains identifier codes and creates a new
	 * resolved codes. Resolving means mapping from code to internal ID. 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <KeyType, ValueType> void createCodeResolvedMap(EMappingType mappingType, EMappingType destMappingType)
	{
		HashMap codeResolvedMap = null;
		
		EIDType originKeyType = mappingType.getTypeOrigin();
		EIDType originValueType = mappingType.getTypeTarget();
		EIDType destKeyType = destMappingType.getTypeOrigin();
		EIDType destValueType = destMappingType.getTypeTarget();
		
		HashMap<KeyType, ValueType> srcMap = (HashMap<KeyType, ValueType>)hashType2Mapping.get(mappingType);
		
		// Remove old unresolved map
		hashType2Mapping.remove(mappingType);
		
		// TODO: implement for multi maps
		
		if (originKeyType == destKeyType)
		{
			if (originValueType != destValueType)
			{
				if (originKeyType.getStorageType() == EStorageType.INT 
						&& destValueType.getStorageType() == EStorageType.INT)
				{
					codeResolvedMap = new HashMap<Integer, Integer>();
				
					//TODO: implement
				}
				else if (originKeyType.getStorageType() == EStorageType.INT 
						&& destValueType.getStorageType() == EStorageType.STRING)
				{
					codeResolvedMap = new HashMap<Integer, String>();
				
					//TODO: implement
				}
				else if (originKeyType.getStorageType() == EStorageType.STRING 
						&& destValueType.getStorageType() == EStorageType.STRING)
				{
					codeResolvedMap = new HashMap<String, String>();
				
					//TODO: implement
				}
				else if (originKeyType.getStorageType() == EStorageType.STRING 
						&& destValueType.getStorageType() == EStorageType.INT)
				{
					codeResolvedMap = new HashMap<String, Integer>();
										
					EMappingType conversionType = EMappingType.valueOf(originValueType
							+ "_2_" + destValueType);

					for (KeyType key : srcMap.keySet())
					{						
						codeResolvedMap.put(key, 
								generalManager.getGenomeIdManager().getID(conversionType, srcMap.get(key)));
					}
				}
			}
		}
		else
		{
			if (originValueType == destValueType)
			{
				if (destKeyType.getStorageType() == EStorageType.INT 
						&& destValueType.getStorageType() == EStorageType.INT)
				{
					codeResolvedMap = new HashMap<Integer, Integer>();

					EMappingType conversionType = EMappingType.valueOf(originKeyType
							+ "_2_" + destKeyType);

					for (KeyType key : srcMap.keySet())
					{						
						codeResolvedMap.put(generalManager.getGenomeIdManager().getID(conversionType, key), 
								srcMap.get(key));
					}
				}
				else if (destKeyType.getStorageType() == EStorageType.INT 
						&& destValueType.getStorageType() == EStorageType.STRING)
				{
					codeResolvedMap = new HashMap<Integer, String>();
				
					//TODO: implement
				}
				else if (destKeyType.getStorageType() == EStorageType.STRING 
						&& destValueType.getStorageType() == EStorageType.STRING)
				{
					codeResolvedMap = new HashMap<String, String>();

					//TODO: implement
				}
				else if (destKeyType.getStorageType() == EStorageType.STRING 
						&& destValueType.getStorageType() == EStorageType.INT)
				{
					codeResolvedMap = new HashMap<String, Integer>();
				
					//TODO: implement
				}
			}	
		}
		
		// Add new code resolved map
		hashType2Mapping.put(destMappingType, codeResolvedMap);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <KeyType, ValueType> HashMap<KeyType, ValueType> getMapping(EMappingType type)
	{
		return (HashMap<KeyType, ValueType>) hashType2Mapping.get(type);
	}

	@Override
	public final boolean hasMapping(EMappingType type)
	{
		return hashType2Mapping.containsKey(type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <KeyType, ValueType> ValueType getID(EMappingType type, KeyType key)
	{
		HashMap<KeyType, ValueType> tmpHashMap = (HashMap<KeyType, ValueType>) hashType2Mapping.get(type);
		return tmpHashMap.get(key);
	}
}
