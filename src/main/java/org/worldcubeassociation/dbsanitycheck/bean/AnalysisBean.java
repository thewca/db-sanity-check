package org.worldcubeassociation.dbsanitycheck.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import lombok.Data;

@Data
public class AnalysisBean {
	private List<JSONObject> analysis;
	private String category;
	private String topic;

	// Since we're using LinkedHashMap, it makes sense to assume that the keys
	// maintain the order
	public List<String> getKeys() {
		List<String> result = new ArrayList<>();
		if (analysis.isEmpty()) { // Extra check. It should not be called
			return result;
		}

		for (String header : analysis.get(0).keySet()) {
			result.add(header);
		}
		return result;
	}
}
