package org.worldcubeassociation.dbsanitycheck.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;

import ch.qos.logback.classic.Logger;

public class QueryReaderTest {

	@InjectMocks
	QueryReader queryReader;
	
	@Before
	public void setup() {
	    MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void read() throws FileNotFoundException, SanityCheckException {
		Logger log = LogUtil.getDefaultLogger(QueryReader.class);

		// We throw an exception if the file does not have categories organized
		ReflectionTestUtils.setField(queryReader, "filename", "./src/test/resources/valid-queries.txt");

		queryReader.read();
		
		List<String> logsCategories = LogUtil.getLogsContaining(log, "categories");
		assertEquals(1,  logsCategories.size());

		// File has 3 categories, This value is logged
		int categoriesFound = Integer.valueOf(logsCategories.get(0).split(" ")[1]);
		assertEquals(3, categoriesFound);
		
		// And 6 queries
		List<String> logsQueries = LogUtil.getLogsContaining(log, "queries");
		int queriesFound = Integer.valueOf(logsQueries.get(0).split(" ")[1]);
		assertEquals(6, queriesFound);
	}

	@Test(expected = SanityCheckException.class)
	public void readCategoriesNotOrganized() throws FileNotFoundException, SanityCheckException {

		// We throw an exception if the file does not have categories organized
		ReflectionTestUtils.setField(queryReader, "filename", "./src/test/resources/categories-not-organized.txt");

		queryReader.read();
	}
	
	@Test(expected = SanityCheckException.class)
	public void readInvalidQuery() throws FileNotFoundException, SanityCheckException {

		/*
		 * All queries should have
		 * 
		 * category
		 * topic
		 * sql query lines
		 */
		ReflectionTestUtils.setField(queryReader, "filename", "./src/test/resources/invalid-queries.txt");

		queryReader.read();
	}
	
	@Test(expected = SanityCheckException.class)
	public void repeatedCategoryTopicQuery() throws FileNotFoundException, SanityCheckException {

		ReflectionTestUtils.setField(queryReader, "filename", "./src/test/resources/repeated-category-topic-queries.txt");

		queryReader.read();
	}
	
	@Test(expected = SanityCheckException.class)
	public void repeatedSqlQuery() throws FileNotFoundException, SanityCheckException {

		ReflectionTestUtils.setField(queryReader, "filename", "./src/test/resources/repeated-sql-queries.txt");

		queryReader.read();
	}

}
