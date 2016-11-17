package it.unige.dibris.batchrmperm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BatchRmPermApplication {
	private static ConfigurableApplicationContext cat;

	public static void main(String[] args) {
		cat = SpringApplication.run(BatchRmPermApplication.class, args);
	}

	public static void close() {
		cat.close();
	}
}
