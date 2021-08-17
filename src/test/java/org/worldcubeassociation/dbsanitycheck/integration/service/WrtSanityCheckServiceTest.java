package org.worldcubeassociation.dbsanitycheck.integration.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.worldcubeassociation.dbsanitycheck.integration.AbstractTest;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;
import org.worldcubeassociation.dbsanitycheck.util.EmailUtil;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class WrtSanityCheckServiceTest extends AbstractTest {

    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    @MockBean
    private JavaMailSender emailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    @BeforeEach
    private void setup() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Must run sanity check")
    @Sql({"/test-scripts/cleanTestData.sql", "/test-scripts/regularWorkflow.sql"})
    public void regularWorkflow() throws MessagingException, IOException {
        wrtSanityCheckService.execute();

        String result = getEmailResult();

        validateHtmlResponse(result);
    }

    private String getEmailResult() throws MessagingException, IOException {
        verify(emailSender, times(1)).send(messageCaptor.capture());

        List<MimeMessage> messages = messageCaptor.getAllValues();

        return EmailUtil.getEmailResult(messages);
    }
}
