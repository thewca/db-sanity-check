package org.worldcubeassociation.dbsanitycheck.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QueryBean {
	private String category;
	private String topic;
	private String query;

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj.getClass() != this.getClass()) {
            return false;
        }
		
		final QueryBean other = (QueryBean) obj;
		
		// First, we compare category and topic
		if (other.getCategory() == null || other.getTopic() == null) {
			return false;
		}
		
		// If they match, returns true
		if (other.getCategory().equals(category) && other.getTopic().equals(topic)) {
			return true;
		}
		
		// Then we compare sql query
		if (other.getQuery() == null) {
			return false;
		}
		
		return other.getQuery().equals(query);
	}
	
	@Override
	public int hashCode() {
		return query.hashCode(); // Not great
	}
}
