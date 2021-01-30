package org.worldcubeassociation.dbsanitycheck.service;

import org.springframework.core.io.ByteArrayResource;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;

import java.util.List;

@FunctionalInterface
public interface ExclusionService {
    ByteArrayResource buildExclusionSuggestionFile(List<AnalysisBean> analysisResult);
}
