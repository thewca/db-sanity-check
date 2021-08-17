package org.worldcubeassociation.dbsanitycheck.integration.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
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
import org.worldcubeassociation.dbsanitycheck.util.GreenMailUtil;

import javax.mail.MessagingException;
import java.io.IOException;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
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

        String result = GreenMailUtil.getEmailResult(greenMail);

        validateHtmlResponse(result);
    }

    @Test
    @DisplayName("Exclusion")
    @Sql({"/test-scripts/cleanTestData.sql", "/test-scripts/exclusion.sql"})
    public void exclusion() throws MessagingException, IOException {
        wrtSanityCheckService.execute();

        String result = GreenMailUtil.getEmailResult(greenMail);

        validateHtmlResponse(result);
    }
}
