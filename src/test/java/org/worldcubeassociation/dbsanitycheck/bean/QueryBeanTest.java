package org.worldcubeassociation.dbsanitycheck.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class QueryBeanTest {

	@Test
	public void equalityQueryBeanCategoryAndTopicTest() {
		QueryBean queryBean1 = new QueryBean();
		queryBean1.setCategory("cat 1");
		queryBean1.setTopic("topic 1");
		queryBean1.setQuery("query 1");
		
		QueryBean queryBean2 = new QueryBean();
		queryBean2.setCategory("cat 1");
		queryBean2.setTopic("topic 1");
		queryBean2.setQuery("query 2"); // The only difference
		
		assertEquals(queryBean1, queryBean2);
	}
	
	@Test
	public void equalityQueryBeanSqlQueryTest() {
		QueryBean queryBean1 = new QueryBean();
		queryBean1.setCategory("cat 1");
		queryBean1.setTopic("topic 1");
		queryBean1.setQuery("query 1");
		
		QueryBean queryBean2 = new QueryBean();
		queryBean2.setCategory("cat 2");
		queryBean2.setTopic("topic 2");
		queryBean2.setQuery("query 1"); // The only equality
		
		assertEquals(queryBean1, queryBean2);
	}
	
	@Test
	public void equalityQueryBeanTest() {
		QueryBean queryBean1 = new QueryBean();
		queryBean1.setCategory("cat 1");
		queryBean1.setTopic("topic 1");
		queryBean1.setQuery("query 1");
		
		QueryBean queryBean2 = new QueryBean();
		queryBean2.setCategory("cat 2");
		queryBean2.setTopic("topic 2");
		queryBean2.setQuery("query 2");
		
		assertNotEquals(queryBean1, queryBean2);
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void generalEqualityTest() {
		QueryBean queryBean1 = new QueryBean();
		queryBean1.setQuery("");
		queryBean1.setTopic("");
		queryBean1.setCategory("");
		
		QueryBean queryBean2 = new QueryBean();
		queryBean2.setCategory("");
		
		QueryBean queryBean3 = new QueryBean();
		queryBean3.setTopic("");
		
		QueryBean queryBeanCategory = new QueryBean();
		queryBeanCategory.setCategory("category");
		queryBeanCategory.setTopic("");
		queryBeanCategory.setQuery("query");
		
		QueryBean queryBeanTopic = new QueryBean();
		queryBeanTopic.setCategory("");
		queryBeanTopic.setTopic("topic");
		queryBeanTopic.setQuery("query");
		
		QueryBean queryBeanCategoryTopic = new QueryBean();
		queryBeanCategoryTopic.setCategory("category");
		queryBeanCategoryTopic.setTopic("topic");
		
		Map<String, String> map = new HashMap<>();
		map.put("category", "");
		map.put("topic", "");
		map.put("query", "");
		
		assertFalse(queryBean1.equals(null));
		assertFalse(queryBean1.equals(queryBean2));
		assertFalse(queryBean1.equals(queryBean3));
		assertFalse(queryBean1.equals(map));
		assertFalse(queryBean1.equals(queryBeanCategory));
		assertFalse(queryBean1.equals(queryBeanTopic));
		assertFalse(queryBean1.equals(queryBeanCategoryTopic));
	}
	
	@Test
	public void hashCodeTest() {
		QueryBean queryBean = new QueryBean();
		queryBean.setQuery("select * from table");
		assertNotNull(queryBean.hashCode());
	}

}
