package org.caleydo.core.command.data.parser;

import java.util.Map;
import java.util.StringTokenizer;

import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.base.ACmdExternalAttributes;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.mapping.IDMappingManager;
import org.caleydo.core.data.mapping.IDMappingManagerRegistry;
import org.caleydo.core.data.mapping.MappingType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.specialized.Organism;
import org.caleydo.core.parser.ascii.LookupTableLoader;
import org.caleydo.core.parser.parameter.ParameterHandler;
import org.caleydo.core.util.conversion.ConversionTools;

/**
 * Command loads lookup table from file using one delimiter and a target Collection.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class CmdLoadFileLookupTable
	extends ACmdExternalAttributes {

	protected String fileName;

	private String sLookupTableInfo;

	private IDCategory idCategory;

	private IDType fromIDType;

	private IDType toIDType;

	/**
	 * Special cases for creating reverse map and using internal LUTs. Valid values are: LUT|LUT_2 REVERSE
	 */
	private String sLookupTableOptions;

	/**
	 * Define type of lookup table to be created.
	 * 
	 * @see org.caleydo.core.data.mapping.EIDType
	 */
	private String sLookupTableDelimiter;

	private int startParsingInLine = 0;

	/**
	 * Default is -1 indicating read till end of file.
	 */
	private int stopParsingInLine = -1;

	private boolean bCreateReverseMap = false;

	/**
	 * Boolean indicates if one column of the mapping needs to be resolved. Resolving means replacing codes by
	 * internal IDs.
	 */
	private boolean bResolveCodeMappingUsingCodeToId_LUT = false;

	/**
	 * Variable contains the lookup table types that are needed to resolve mapping tables that contain codes
	 * instead of internal IDs.
	 */
	private String sCodeResolvingLUTTypes;

	private String sCodeResolvingLUTMappingType;

	private boolean isMultiMap;

	// private ATableBasedDataDomain dataDomain;

	/**
	 * Constructor.
	 * 
	 * @param cmdType
	 */
	public CmdLoadFileLookupTable() {
		super(CommandType.LOAD_LOOKUP_TABLE_FILE);
	}

	@Override
	public void setParameterHandler(final ParameterHandler parameterHandler) {

		super.setParameterHandler(parameterHandler);

		fileName = detail;
		sLookupTableInfo = attrib1;
		sLookupTableDelimiter = attrib2;

		if (attrib3 != null) {
			int[] iArrayStartStop = ConversionTools.convertStringToIntArray(attrib3, " ");

			if (iArrayStartStop.length == 2) {
				startParsingInLine = iArrayStartStop[0];
				stopParsingInLine = iArrayStartStop[1];
			}
		}

		sCodeResolvingLUTTypes = attrib4;

		isMultiMap = Boolean.parseBoolean(attrib5);

		idCategory = IDCategory.getIDCategory(attrib6);

		extractParameters();
	}

	public void setAttributes(final String sFileName, final int startParsingInLine,
		final int stopParsingInLine, final String sLookupTableInfo, final String sLookupTableDelimiter,
		final String sCodeResolvingLUTTypes, final IDCategory idCategory) {

		this.startParsingInLine = startParsingInLine;
		this.stopParsingInLine = stopParsingInLine;
		this.sLookupTableInfo = sLookupTableInfo;
		this.sLookupTableDelimiter = sLookupTableDelimiter;
		this.sCodeResolvingLUTTypes = sCodeResolvingLUTTypes;
		this.fileName = sFileName;
		this.idCategory = idCategory;
		extractParameters();
	}

	private void extractParameters() {

		StringTokenizer tokenizer =
			new StringTokenizer(sLookupTableInfo, GeneralManager.sDelimiter_Parser_DataItems);

		String mappingTypeString = tokenizer.nextToken();
		fromIDType = IDType.getIDType(mappingTypeString.substring(0, mappingTypeString.indexOf("_2_")));
		toIDType =
			IDType.getIDType(mappingTypeString.substring(mappingTypeString.indexOf("_2_") + 3,
				mappingTypeString.length()));

		while (tokenizer.hasMoreTokens()) {
			sLookupTableOptions = tokenizer.nextToken();

			if (sLookupTableOptions.equals("REVERSE")) {
				bCreateReverseMap = true;
			}
			else if (sLookupTableOptions.equals("LUT")) {
				tokenizer =
					new StringTokenizer(sCodeResolvingLUTTypes, GeneralManager.sDelimiter_Parser_DataItems);

				sCodeResolvingLUTMappingType = tokenizer.nextToken();

				bResolveCodeMappingUsingCodeToId_LUT = true;
			}
		}
	}

	/**
	 * Load data from file using a token pattern.
	 * 
	 * @see org.caleydo.core.command.ICommand#doCommand()
	 */
	@Override
	public void doCommand() {
		LookupTableLoader loader = null;

		if (fileName.contains("ORGANISM")) {
			Organism eOrganism = GeneralManager.get().getBasicInfo().getOrganism();
			this.fileName = fileName.replace("ORGANISM", eOrganism.toString());
		}

		// FIXME: Currently we do not have the ensembl mapping table for home sapiens
		if (fileName.contains("HOMO_SAPIENS") && fileName.contains("ENSEMBL"))
			return;

		// Remove old lookuptable if it already exists
		// genomeIdManager.removeMapByType(EMappingType.valueOf(sLookupTableType));
		if(idCategory == null)
			throw new IllegalStateException("ID Category was null");
		IDMappingManager genomeIdManager = IDMappingManagerRegistry.get().getIDMappingManager(idCategory);
		MappingType mappingType = genomeIdManager.createMap(fromIDType, toIDType, isMultiMap);

		if (bResolveCodeMappingUsingCodeToId_LUT) {

			IDType codeResolvedFromIDType =
				IDType.getIDType(sCodeResolvingLUTMappingType.substring(0,
					sCodeResolvingLUTMappingType.indexOf("_2_")));
			IDType codeResolvedToIDType =
				IDType.getIDType(sCodeResolvingLUTMappingType.substring(
					sCodeResolvingLUTMappingType.indexOf("_2_") + 3, sCodeResolvingLUTMappingType.length()));

			genomeIdManager.createCodeResolvedMap(mappingType, codeResolvedFromIDType, codeResolvedToIDType);
		}

		int index = 0;
		if (fileName.equals("generate")) {

			Map<String, Integer> hashTmp = genomeIdManager.getMap(mappingType);
			for (Object refSeqIDObject : genomeIdManager.getMap(
				genomeIdManager.getMappingType("DAVID_2_REFSEQ_MRNA")).values()) {

				hashTmp.put((String) refSeqIDObject, index++);
			}
		}
		else if (!fileName.equals("already_loaded")) {
			loader = new LookupTableLoader(idCategory, fileName, mappingType);
			loader.setTokenSeperator(sLookupTableDelimiter);
			loader.setStartParsingStopParsingAtLine(startParsingInLine, stopParsingInLine);
			loader.loadData();
		}

		/* --- create reverse Map ... --- */
		if (bCreateReverseMap) {
			genomeIdManager.createReverseMap(mappingType);
		}
	}

	@Override
	public void undoCommand() {
	}
}
