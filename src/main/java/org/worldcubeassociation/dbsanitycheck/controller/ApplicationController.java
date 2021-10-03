package org.worldcubeassociation.dbsanitycheck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

@Controller
@Profile("!test")
public class ApplicationController implements CommandLineRunner {

    @Autowired
    private WrtSanityCheckService wrtSanityCheckService;

    @Override
    public void run(String... args) throws Exception {
        wrtSanityCheckService.execute();
    }
}
