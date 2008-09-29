package org.caleydo.core.command.data.parser;

import java.util.StringTokenizer;
import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.base.ACommand;
import org.caleydo.core.data.mapping.EMappingDataType;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.parser.ascii.lookuptable.ALookupTableLoader;
import org.caleydo.core.parser.parameter.IParameterHandler;
import org.caleydo.core.util.system.StringConversionTool;

/**
 * Command loads lookup table from file using one delimiter and a target
 * Collection.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class CmdLoadFileLookupTable
	extends ACommand
{

	protected String sFileName;

	private String sLookupTableInfo;

	protected String sLookupTableType;

	/**
	 * Special cases for creating reverse map and using internal LUTs. Valid
	 * values are: LUT|LUT_2 REVERSE
	 */
	protected String sLookupTableOptions;

	/**
	 * Define type of lookup table to be created.
	 * 
	 * @see org.caleydo.core.data.mapping.EIDType
	 */
	protected String sLookupTableDelimiter;

	protected int iStartPareseFileAtLine = 0;

	/**
	 * Default is -1 indicating read till end of file.
	 * 
	 * @see org.caleydo.core.parser.ascii.microarray.MicroArrayLoader1Storage#iStopParsingAtLine
	 * @see org.caleydo.core.parser.ascii.microarray.MicroArrayLoader1Storage#getStopParsingAtLine()
	 * @see org.caleydo.core.parser.ascii.microarray.MicroArrayLoader1Storage#setStartParsingStopParsingAtLine(int,
	 *      int)
	 */
	protected int iStopParseFileAtLine = -1;

	protected boolean bCreateReverseMap = false;

	/**
	 * Boolean indicates if one column of the mapping needs to be resolved.
	 * Resolving means replacing codes by internal IDs.
	 */
	protected boolean bResolveCodeMappingUsingCodeToId_LUT_1 = false;

	protected boolean bResolveCodeMappingUsingCodeToId_LUT_2 = false;

	/**
	 * Boolean indicates if both columns of the mapping needs to be resolved.
	 * Resolving means replacing codes by internal IDs.
	 */
	protected boolean bResolveCodeMappingUsingCodeToId_LUT_BOTH = false;

	/**
	 * Variable contains the lookup table types that are needed to resolve
	 * mapping tables that contain codes instead of internal IDs.
	 */
	protected String sCodeResolvingLUTTypes;

	protected String sCodeResolvingLUTMappingType_1;

	protected String sCodeResolvingLUTMappingType_2;

	/**
	 * Constructor.
	 * 
	 * @param cmdType
	 */
	public CmdLoadFileLookupTable(final ECommandType cmdType)
	{
		super(cmdType);
	}

	@Override
	public void setParameterHandler(final IParameterHandler parameterHandler)
	{
		sFileName = parameterHandler.getValueString(ECommandType.TAG_DETAIL.getXmlKey());

		sLookupTableInfo = parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE1
				.getXmlKey());

		sLookupTableDelimiter = parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE2
				.getXmlKey());

		int[] iArrayStartStop = StringConversionTool.convertStringToIntArrayVariableLength(
				parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE3.getXmlKey()), " ");

		iStartPareseFileAtLine = iArrayStartStop[0];
		iStopParseFileAtLine = iArrayStartStop[1];

		sCodeResolvingLUTTypes = parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE4
				.getXmlKey());

		extractParameters();
	}

	public void setAttributes(final String sFileName, final int iStartParseFileAtLine,
			final int iStopParseFileAtLine, final String sLookupTableInfo,
			final String sLookupTableDelimiter, final String sCodeResolvingLUTTypes)
	{

		this.sFileName = sFileName;
		this.iStartPareseFileAtLine = iStartParseFileAtLine;
		this.iStopParseFileAtLine = iStopParseFileAtLine;
		this.sLookupTableInfo = sLookupTableInfo;
		this.sLookupTableDelimiter = sLookupTableDelimiter;
		this.sCodeResolvingLUTTypes = sCodeResolvingLUTTypes;

		extractParameters();
	}

	private void extractParameters()
	{

		StringTokenizer tokenizer = new StringTokenizer(sLookupTableInfo,
				IGeneralManager.sDelimiter_Parser_DataItems);

		sLookupTableType = tokenizer.nextToken();

		while (tokenizer.hasMoreTokens())
		{
			sLookupTableOptions = tokenizer.nextToken();

			if (sLookupTableOptions.equals("REVERSE"))
			{
				bCreateReverseMap = true;
			}
			else if (sLookupTableOptions.equals("LUT_1")
					|| sLookupTableOptions.equals("LUT_2")
					|| sLookupTableOptions.equals("LUT_BOTH"))
			{
				tokenizer = new StringTokenizer(sCodeResolvingLUTTypes,
						IGeneralManager.sDelimiter_Parser_DataItems);

				sCodeResolvingLUTMappingType_1 = tokenizer.nextToken();
//				
				if (sLookupTableOptions.equals("LUT_1"))
				{
//					sCodeResolvingLUTMappingType_1 = tokenizer.nextToken();
					bResolveCodeMappingUsingCodeToId_LUT_1 = true;
				}
				else if (sLookupTableOptions.equals("LUT_2"))
				{
//					sCodeResolvingLUTMappingType_2 = tokenizer.nextToken();
					bResolveCodeMappingUsingCodeToId_LUT_2 = true;
				}
				else if (sLookupTableOptions.equals("LUT_BOTH"))
				{
//					sCodeResolvingLUTMappingType_1 = tokenizer.nextToken();
//					sCodeResolvingLUTMappingType_2 = tokenizer.nextToken();
//
					bResolveCodeMappingUsingCodeToId_LUT_BOTH = true;
				}
			}
		}
	}

	/**
	 * Load data from file using a token pattern.
	 * 
	 * @see org.caleydo.core.parser.ascii.microarray.MicroArrayLoader1Storage#loadData()
	 * @see org.caleydo.core.command.ICommand#doCommand()
	 */
	public void doCommand()
	{
		ALookupTableLoader loader = null;

		IIDMappingManager genomeIdManager = generalManager.getGenomeIdManager();

		// Remove old lookuptable if it already exists
//		genomeIdManager.removeMapByType(EMappingType.valueOf(sLookupTableType));

		EMappingType mappingType = EMappingType.valueOf(sLookupTableType);

		EMappingDataType dataType;

		dataType = mappingType.getDataMapppingType();

		// FIXME: find solution for lut resolve process
		if (bResolveCodeMappingUsingCodeToId_LUT_BOTH)
		{
			if (dataType == EMappingDataType.INT2INT)
			{
				dataType = EMappingDataType.STRING2STRING;
			}
			else if (dataType == EMappingDataType.MULTI_INT2INT)
			{
				dataType = EMappingDataType.MULTI_STRING2STRING;
			}
		}
		else if (bResolveCodeMappingUsingCodeToId_LUT_1)
		{
			if (dataType == EMappingDataType.INT2STRING)
			{
				dataType = EMappingDataType.STRING2STRING;
			}
			else if (dataType == EMappingDataType.INT2INT)
			{
				dataType = EMappingDataType.STRING2INT;
			}
		}
		else if (bResolveCodeMappingUsingCodeToId_LUT_2)
		{
			if (dataType == EMappingDataType.STRING2INT)
			{
				dataType = EMappingDataType.STRING2STRING;
			}
			else if (dataType == EMappingDataType.INT2INT)
			{
				dataType = EMappingDataType.INT2STRING;
			}
		}
		
		loader = new ALookupTableLoader(sFileName, mappingType, dataType);

		loader.setTokenSeperator(sLookupTableDelimiter);

		// if ( sFileName.endsWith( sCommaSeperatedFileExtension )) {
		// loader.setTokenSeperator(
		// IGeneralManager.sDelimiter_Parser_DataType );
		// }

		loader.setStartParsingStopParsingAtLine(iStartPareseFileAtLine,
				iStopParseFileAtLine);

		loader.loadData();

		/* --- Map codes in LUT to IDs --- */
		if (bResolveCodeMappingUsingCodeToId_LUT_1
				|| bResolveCodeMappingUsingCodeToId_LUT_2
				|| bResolveCodeMappingUsingCodeToId_LUT_BOTH)
		{
//				EMappingType genomeMappingLUT_1 = null;
//				EMappingType genomeMappingLUT_2 = null;
//
//				if (bResolveCodeMappingUsingCodeToId_LUT_1
//						|| bResolveCodeMappingUsingCodeToId_LUT_BOTH)
//				{
//					genomeMappingLUT_1 = EMappingType.valueOf(sCodeResolvingLUTMappingType_1);
//				}
//
//				if (bResolveCodeMappingUsingCodeToId_LUT_2
//						|| bResolveCodeMappingUsingCodeToId_LUT_BOTH)
//				{
//					genomeMappingLUT_2 = EMappingType.valueOf(sCodeResolvingLUTMappingType_2);
//				}

			genomeIdManager.createCodeResolvedMap(mappingType, 
					EMappingType.valueOf(sCodeResolvingLUTMappingType_1));
			
//				if (dataType == EMappingDataType.MULTI_INT2INT)
//				{
////					loader.createCodeResolvedMultiMapFromMultiMapString(generalManager,
////							mappingType, genomeMappingLUT_1, genomeMappingLUT_2);
//				}
//				else
//				{
////					loader.createCodeResolvedMapFromMap(generalManager, mappingType,
////							genomeMappingLUT_1, genomeMappingLUT_2, targetMappingDataType);
//				}
		}

		/* --- create reverse Map ... --- */
		if (bCreateReverseMap)
		{
			if (sCodeResolvingLUTMappingType_1 != null)
			{
				mappingType = EMappingType.valueOf(sCodeResolvingLUTMappingType_1);
			}
				
			// Concatenate genome id type target and origin type in swapped
			// order to determine reverse genome mapping type.
			EMappingType reverseMappingType = EMappingType.valueOf(
					mappingType.getTypeTarget().toString()
					+ "_2_" + mappingType.getTypeOrigin().toString());

			genomeIdManager.createReverseMap(mappingType, reverseMappingType);
			
//				if (reverseMappingType.isMultiMap())
//				{
//					switch (reverseMappingType.getTypeOrigin().getStorageType())
//					{
//						case INT:
//							loader.createReverseMultiMap(generalManager,
//									mappingType, reverseMappingType);
//							break;
//
//						case STRING:
////							loader.createReverseMultiMapFromMultiMapString(generalManager,
////									lut_genome_type, lut_genome_reverse_type);
//							break;
//
//						default:
//							throw new RuntimeException(
//									"Reverse mapping not suported yet for this type="
//											+ reverseMappingType.toString());
//
//					}
//				}
//				else
//				{
////					loader.createReverseMapFromMap(lut_genome_type, lut_genome_reverse_type);
//				}
		}

		commandManager.runDoCommand(this);
	}

	@Override
	public void undoCommand()
	{

		commandManager.runUndoCommand(this);
	}
}
