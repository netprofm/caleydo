/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.data.importer.tcga.qualitycontrol;

import org.caleydo.data.importer.tcga.Settings;
import org.kohsuke.args4j.Option;


public class TCGAQCSettings extends Settings {
	@Option(name = "-s", aliases = { "--server" }, usage = "TCGA Server URL that hosts TCGA Caleydo project files default: \"http://compbio.med.harvard.edu/tcga/stratomex/data_qc/\"")
	private String tcgaServerURL = "http://compbio.med.harvard.edu/tcga/stratomex/data_qc/";

	public String getTcgaServerURL() {
		return tcgaServerURL;
	}
}
