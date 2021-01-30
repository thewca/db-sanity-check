package org.worldcubeassociation.dbsanitycheck.service.impl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.service.ExclusionService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExclusionServiceImpl implements ExclusionService {
    @Override
    public ByteArrayResource buildExclusionSuggestionFile(List<AnalysisBean> analysisResult) {
        String textFile = "";
        for (int i = 0; i < analysisResult.size(); i++) {
            textFile += analysisToInsert(analysisResult.get(i), i) + "\n\n\n";
        }
        return new ByteArrayResource(textFile.getBytes(StandardCharsets.UTF_8));
    }

    private String analysisToInsert(AnalysisBean analysis, int index) {
        int sanityCheckId = analysis.getSanityCheck().getId();

        String identifier =
                String.format("-- %s. [%s] %s", index + 1, analysis.getSanityCheck().getCategory().getName(),
                        analysis.getSanityCheck().getTopic());

        // Empty comment so WRT can easily remember to change this
        String suggestion = analysis.getAnalysis().stream().map(it -> String
                .format("INSERT INTO sanity_check_exclusions (sanity_check_id, exclusion, comments) values (%s, '%s',"
                                + " %s);",
                        sanityCheckId, it.toString().replaceAll("'", "\\\\'"), "''")).collect(Collectors.joining("\n"));

        return identifier + "\n\n" + suggestion;
    }
}
