package org.worldcubeassociation.dbsanitycheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DbSanityCheckApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(DbSanityCheckApplication.class, args)));
	}

}
