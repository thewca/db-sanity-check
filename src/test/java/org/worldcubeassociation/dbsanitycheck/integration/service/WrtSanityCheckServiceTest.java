package org.worldcubeassociation.dbsanitycheck.integration.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import javax.mail.MessagingException;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class WrtSanityCheckServiceTest {
    
    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    @Test
    @DisplayName("Must run sanity check")
    @Sql({"/test-scripts/cleanTestData.sql", "/test-scripts/regularWorkflow.sql"})
    public void regularWorkflow() throws MessagingException {
        wrtSanityCheckService.execute();
    }
}
