package org.worldcubeassociation.dbsanitycheck.service.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.util.StubUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

public class ExclusionServiceImplTest {

    @InjectMocks
    private ExclusionServiceImpl sanityCheckExclusionService;

    private static final Random RANDOM = new Random();

    private static final int MAX_COLUMNS = 5;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildExclusionSuggestionFileTest() throws JSONException {
        List<AnalysisBean> analysis = new ArrayList<>();
        int nResults = 50;

        for (int i = 0; i < nResults; i++) {
            AnalysisBean analysisBean = new AnalysisBean();
            analysisBean.setSanityCheck(StubUtil.getDefaultSanityCheck(i));

            analysisBean.setAnalysis(getDefaultAnalysis());

            analysis.add(analysisBean);
        }
        assertNotNull(sanityCheckExclusionService.buildExclusionSuggestionFile(analysis));
    }

    private List<JSONObject> getDefaultAnalysis() throws JSONException {
        List<JSONObject> result = new ArrayList<>();

        int nResults = 1 + RANDOM.nextInt(10);
        int columns = 1 + RANDOM.nextInt(MAX_COLUMNS);
        for (int i = 0; i < nResults; i++) {
            JSONObject json = new JSONObject();

            for (int j = 0; j < columns; j++) {
                json.put("column " + j, "result " + j);
            }

            result.add(json);
        }
        return result;
    }
}
