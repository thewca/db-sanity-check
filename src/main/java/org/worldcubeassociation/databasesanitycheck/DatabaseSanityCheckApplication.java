package org.worldcubeassociation.databasesanitycheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatabaseSanityCheckApplication {

	public static void main(String[] args) throws Exception {
		System.exit(SpringApplication.exit(SpringApplication.run(DatabaseSanityCheckApplication.class, args)));
	}

}
