package org.caleydo.view.treemap.layout;

import java.lang.reflect.Method;

import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.util.mapping.color.ColorMapping;

public class ClusterReferenzData{
	float sizeReferenzValue=1;
	float colorReferenzSpace=1;
	float colorMin;
	float colorMax;
	ColorMapping colorMapper;
	
	boolean bUseExpressionValues = false;
	ASetBasedDataDomain dataDomain;
}