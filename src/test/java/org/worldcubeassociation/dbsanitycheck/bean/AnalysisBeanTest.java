package org.worldcubeassociation.dbsanitycheck.bean;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class AnalysisBeanTest {

	@Test
	public void emptyKeysTest() {
		// Extra check. This should not happen, but if it does,
		// things will not break
		AnalysisBean analysisBean = new AnalysisBean();
		analysisBean.setAnalysis(new ArrayList<>());
		assertTrue(analysisBean.getKeys().isEmpty());
	}

}
