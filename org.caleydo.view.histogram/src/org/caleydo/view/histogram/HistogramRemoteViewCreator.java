/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.histogram;

import java.util.List;

import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.IRemoteViewCreator;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;

public class HistogramRemoteViewCreator implements IRemoteViewCreator {

	public HistogramRemoteViewCreator() {
	}

	@Override
	public AGLView createRemoteView(AGLView remoteRenderingView, List<TablePerspective> tablePerspectives,
			String embeddingEventSpace) {
		GLHistogram histogramView = (GLHistogram) ViewManager.get()
				.createGLView(GLHistogram.class, remoteRenderingView.getParentGLCanvas(),
						new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0, 1, 0, 1, -1, 1));

		histogramView.setRemoteRenderingGLView((IGLRemoteRenderingView) remoteRenderingView);
		TablePerspective tablePerspective = null;
		if (tablePerspectives.size() > 0) {
			tablePerspective = tablePerspectives.get(0);

			ATableBasedDataDomain dataDomain = tablePerspective.getDataDomain();

			Histogram histogram = null;

			histogram = tablePerspective.getContainerStatistics().getHistogram();

			histogramView.setDataDomain(dataDomain);
			histogramView.setHistogram(histogram);
			histogramView.setTablePerspective(tablePerspective);
		}
		histogramView.initialize();
		histogramView.setDetailLevel(EDetailLevel.LOW);

		// Ctable.getContentData(Set.CONTENT)
		// if (recordVA != null)
		// histogram.setRecordVA(recordVA);

		return histogramView;
	}

}
