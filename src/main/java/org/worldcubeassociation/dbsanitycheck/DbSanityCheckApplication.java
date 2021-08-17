package org.worldcubeassociation.dbsanitycheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

@Slf4j
@SpringBootApplication
public class DbSanityCheckApplication implements CommandLineRunner {

    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    public static void main(String[] args) {
        SpringApplication.run(DbSanityCheckApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        wrtSanityCheckService.execute();
    }
}
