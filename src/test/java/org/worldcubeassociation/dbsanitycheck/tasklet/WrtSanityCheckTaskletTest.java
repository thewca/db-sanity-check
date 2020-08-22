package org.worldcubeassociation.dbsanitycheck.tasklet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.reader.QueryReader;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;

public class WrtSanityCheckTaskletTest {

	@InjectMocks
	private WrtSanityCheckTasklet wrtSanityCheckTasklet;

	@Mock
	private StepContribution contribution;

	@Mock
	private ChunkContext chunkContext;

	@Mock
	private QueryReader queryReader;

	@Mock
	private EmailService emailService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void executeTest() throws FileNotFoundException, SanityCheckException, MessagingException {
		when(queryReader.read()).thenReturn(getDefaultQueries());

		RepeatStatus status = wrtSanityCheckTasklet.execute(contribution, chunkContext);
		
		assertEquals(RepeatStatus.FINISHED, status);
	}

	private List<QueryBean> getDefaultQueries() {
		List<QueryBean> result = new ArrayList<>();
		return result;
	}

}
