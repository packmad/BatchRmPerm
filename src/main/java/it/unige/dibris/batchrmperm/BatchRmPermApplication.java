package it.unige.dibris.batchrmperm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BatchRmPermApplication {
	private static ConfigurableApplicationContext cat;

	public static void main(String[] args) {
		ConfigurableApplicationContext cat = SpringApplication.run(BatchRmPermApplication.class, args);
	}

	public static void close() {
		cat.close();
	}
}
