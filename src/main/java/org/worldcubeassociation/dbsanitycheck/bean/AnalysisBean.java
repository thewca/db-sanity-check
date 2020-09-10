package org.worldcubeassociation.dbsanitycheck.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisBean extends BaseBean {
	private List<Map<String, String>> analysis;

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
