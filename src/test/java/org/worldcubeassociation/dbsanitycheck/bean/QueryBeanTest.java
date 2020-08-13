package org.worldcubeassociation.dbsanitycheck.bean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QueryBeanTest {

	@Test
	void equalityQueryBeanCategoryAndTopicTest() {
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
	void equalityQueryBeanSqlQueryTest() {
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
	void equalityQueryBeanTest() {
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

}
