package org.worldcubeassociation.dbsanitycheck.integration;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.yml")
public class AbstractIntegrationTest {
}
