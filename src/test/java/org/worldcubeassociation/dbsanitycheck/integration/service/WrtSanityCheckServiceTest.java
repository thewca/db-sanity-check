package org.worldcubeassociation.dbsanitycheck.integration.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.Store;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.worldcubeassociation.dbsanitycheck.integration.AbstractTest;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class WrtSanityCheckServiceTest extends AbstractTest {

    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email@wca.com";

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser(EMAIL, USERNAME, PASSWORD)).withPerMethodLifecycle(false);

    @Test
    @DisplayName("Must run sanity check")
    @Sql({"/test-scripts/cleanTestData.sql", "/test-scripts/regularWorkflow.sql"})
    public void regularWorkflow() throws MessagingException, IOException {
        wrtSanityCheckService.execute();

        String result = getEmailResult();

        validateHtmlResponse(result);
    }

    @Test
    @DisplayName("Query with error")
    @Sql({"/test-scripts/cleanTestData.sql", "/test-scripts/queryWithError.sql"})
    public void queryWithError() throws MessagingException, IOException {
        wrtSanityCheckService.execute();

        String result = getEmailResult();

        validateHtmlResponse(result);
    }

    private String getEmailResult() throws MessagingException, IOException {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        // Email content is just the first email of the current run
        MimeMultipart mimeMultipart = (MimeMultipart) receivedMessages[0].getContent();

        return getTextFromMimeMultipart(mimeMultipart);

    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
        // Adapted from
        // https://stackoverflow.com/questions/11240368/how-to-read-text-inside-body-of-mail-using-javax-mail

        int count = mimeMultipart.getCount();
        if (count == 0)
            throw new MessagingException("Multipart with no body parts not supported.");
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
        if (multipartAlt)
            // alternatives appear in an order of increasing
            // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));

        // Index 0 is the email part
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);
        return getTextFromBodyPart(bodyPart);
    }

    private String getTextFromBodyPart(
            BodyPart bodyPart) throws IOException, MessagingException {

        String result = "";
        if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = html;
        } else if (bodyPart.getContent() instanceof MimeMultipart) {
            result = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }
        return result;
    }
}
