package org.caleydo.core.command.data.parser;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.base.ACommand;
import org.caleydo.core.data.collection.set.LoadDataParameters;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.parser.ascii.TabularAsciiDataReader;
import org.caleydo.core.parser.parameter.ParameterHandler;
import org.caleydo.core.util.conversion.ConversionTools;
import org.caleydo.core.util.logging.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Command loads data from file using a token pattern and a target ISet. Use AMicroArrayLoader to load data
 * set.
 * 
 * @author Marc Streit
 */
public class CmdLoadFileNStorages
	extends ACommand {

	private ArrayList<Integer> storageIDs;

	private LoadDataParameters loadDataParameters;

	private boolean bParsingOK = false;

	ASetBasedDataDomain dataDomain;

	/**
	 * Constructor.
	 */
	public CmdLoadFileNStorages() {
		super(CommandType.LOAD_DATA_FILE);
		loadDataParameters = new LoadDataParameters();
	}

	@Override
	public void setParameterHandler(final ParameterHandler parameterHandler) {
		super.setParameterHandler(parameterHandler);

		loadDataParameters.setFileName(parameterHandler.getValueString(CommandType.TAG_DETAIL.getXmlKey()));
		// this.sTreeFileName = parameterHandler.getValueString(ECommandType.TAG_DETAIL.getXmlKey());
		loadDataParameters.setInputPattern(parameterHandler.getValueString(CommandType.TAG_ATTRIBUTE1
			.getXmlKey()));

		StringTokenizer tokenizer =
			new StringTokenizer(parameterHandler.getValueString(CommandType.TAG_ATTRIBUTE2.getXmlKey()),
				GeneralManager.sDelimiter_Parser_DataItems);

		storageIDs = new ArrayList<Integer>();

		while (tokenizer.hasMoreTokens()) {
			storageIDs.add(Integer.valueOf(tokenizer.nextToken()).intValue());
		}

		// Convert external IDs from XML file to internal IDs
		storageIDs = GeneralManager.get().getIDCreator().convertExternalToInternalIDs(storageIDs);

		int[] iArrayStartStop =
			ConversionTools.convertStringToIntArray(
				parameterHandler.getValueString(CommandType.TAG_ATTRIBUTE3.getXmlKey()), " ");

		if (iArrayStartStop.length > 0) {
			loadDataParameters.setStartParseFileAtLine(iArrayStartStop[0]);

			if (iArrayStartStop.length > 1) {

				if (iArrayStartStop[0] > iArrayStartStop[1] && iArrayStartStop[1] != -1) {
					Logger
						.log(new Status(IStatus.ERROR, this.toString(), "Ignore stop inde="
							+ iArrayStartStop[1] + " because it is maller that start index="
							+ iArrayStartStop[0]));

					return;
				}
				loadDataParameters.setStopParseFileAtLine(iArrayStartStop[1]);
			}
		}

		dataDomain =
			(ASetBasedDataDomain) DataDomainManager.get().getDataDomainByID(
				parameterHandler.getValueString(CommandType.TAG_ATTRIBUTE4.getXmlKey()));
		loadDataParameters.setDataDomain(dataDomain);

	}

	public void setAttributes(final ArrayList<Integer> iAlStorageId,
		final LoadDataParameters loadDataParameters) {
		this.dataDomain = loadDataParameters.getDataDomain();
		this.storageIDs = iAlStorageId;
		this.loadDataParameters = loadDataParameters;
	}

	@Override
	public void doCommand() {
		Logger.log(
			new Status(IStatus.INFO, GeneralManager.PLUGIN_ID, "Loading data from file "
				+ loadDataParameters.getFileName() + " using token pattern "
				+ loadDataParameters.getInputPattern() + ". Data is stored in Storage with ID "
				+ storageIDs.toString()));

		TabularAsciiDataReader loader =
			new TabularAsciiDataReader(loadDataParameters.getFileName(), dataDomain);
		loader.setTokenPattern(loadDataParameters.getInputPattern());
		loader.setTargetStorages(storageIDs);
		if (loadDataParameters.isUseExperimentClusterInfo())
			loader.enableExperimentClusterInfo();
		loader.setStartParsingStopParsingAtLine(loadDataParameters.getStartParseFileAtLine(),
			loadDataParameters.getStopParseFileAtLine());

		if (loadDataParameters.getDelimiter() != null && !loadDataParameters.getDelimiter().isEmpty()) {
			loader.setTokenSeperator(loadDataParameters.getDelimiter());
		}

		bParsingOK = loader.loadData();

		dataDomain.setLoadDataParameters(loadDataParameters);

		dataDomain.updateSetInViews();
	}

	@Override
	public void undoCommand() {

	}

	public boolean isParsingOK() {
		return bParsingOK;
	}
}
