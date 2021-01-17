package org.worldcubeassociation.dbsanitycheck.service;

import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;

import java.io.ByteArrayInputStream;
import java.util.List;

@FunctionalInterface
public interface SanityCheckExclusionService {
    ByteArrayInputStream buildExclusionSuggestionFile(List<AnalysisBean> analysisResult);
}
