package org.worldcubeassociation.dbsanitycheck.bean;

import lombok.Data;
import org.json.JSONObject;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class AnalysisBean {
    private List<JSONObject> analysis;
    private SanityCheck sanityCheck;

    public List<String> getKeys() {
        List<String> result = new ArrayList<>();
        if (analysis.isEmpty()) { // Extra check. It should not be called
            return result;
        }

        Iterator<String> headers = analysis.get(0).keys();
        while (headers.hasNext()) {
            result.add(headers.next());
        }
        return result;
    }
}
