package org.worldcubeassociation.dbsanitycheck.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AnalysisBean {
	private List<Map<String, String>> analysis;
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
