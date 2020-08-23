package org.worldcubeassociation.dbsanitycheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class DbSanityCheckApplication implements CommandLineRunner {
	
	@Autowired
	private WrtSanityCheckService wrtSanityCheckService; 

	@Autowired
	private ConfigurableApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(DbSanityCheckApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		wrtSanityCheckService.execute();
		
		SpringApplication.exit(context, () -> {
			log.info("Finalizando aplicacao com codigo de saida 0");
			return 0;
		});
	}

}
