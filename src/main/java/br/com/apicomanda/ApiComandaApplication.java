package br.com.apicomanda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ApiComandaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiComandaApplication.class, args);
	}

}
