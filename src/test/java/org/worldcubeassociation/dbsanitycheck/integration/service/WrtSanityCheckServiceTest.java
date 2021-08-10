package org.worldcubeassociation.dbsanitycheck.integration.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.worldcubeassociation.dbsanitycheck.integration.AbstractIntegrationTest;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import javax.mail.MessagingException;

@Slf4j
public class WrtSanityCheckServiceTest extends AbstractIntegrationTest {
    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    @Test
    @DisplayName("Must create batch process")
    public void mustExecute() throws MessagingException {
        wrtSanityCheckService.execute();
    }
}
