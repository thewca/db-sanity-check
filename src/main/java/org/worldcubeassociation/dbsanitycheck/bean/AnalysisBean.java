package org.worldcubeassociation.dbsanitycheck.bean;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisBean extends BaseBean {
	private List<Map<String, String>> analysis;
}
