package org.worldcubeassociation.dbsanitycheck.service.impl;

import org.springframework.stereotype.Service;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.service.SanityCheckExclusionService;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SanityCheckExclusionServiceImpl implements SanityCheckExclusionService {
    @Override
    public ByteArrayInputStream buildExclusionSuggestionFile(List<AnalysisBean> analysisResult) {
        String textFile = analysisResult.stream().map(this::analysisToInsert).collect(Collectors.joining("\n\n"));
        return new ByteArrayInputStream(textFile.getBytes(StandardCharsets.UTF_8));
    }

    private String analysisToInsert(AnalysisBean analysis) {
        int sanityCheckId = analysis.getSanityCheck().getId();

        // Empty comment so WRT can easily remember to change this
        return analysis.getAnalysis().stream().map(it -> String
                .format("INSERT INTO sanity_check_exclusions (sanity_check_id, exclusion, comments) values (%s, %s, "
                                + "%s)",
                        sanityCheckId, "")).collect(Collectors.joining("\n"));
    }
}
