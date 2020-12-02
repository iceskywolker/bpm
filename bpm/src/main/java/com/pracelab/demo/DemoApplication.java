package com.pracelab.demo;

import com.pracelab.demo.services.PraceLabFlow;
import org.activiti.cloud.starter.rb.configuration.ActivitiRuntimeBundle;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ActivitiRuntimeBundle
@EnableScheduling
public class DemoApplication implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	@Autowired
	private PraceLabFlow service;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			logger.info("Miejsce na aktywację mechanizmow zaraz po starcie...");
			service.testPE();
			AuthzClient authzClient = AuthzClient.create();

			AuthorizationRequest request = new AuthorizationRequest();
//			request.addPermission("Default Resource");

//			AuthorizationResponse response = authzClient.authorization("modeler", "password").authorize(request);
//			AuthorizationResponse response = authzClient.authorization("bpm-account", "b978cda1-3e5a-4785-b26b-d9d8f5b852ea").authorize(request);
//			String rpt = response.getToken();

//			System.out.println("You got an RPT: " + rpt);
		}
		catch(Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}


	/**
	 * Cykliczne wywolywana metoda, mozna uzyc jako TIMER
	 */
	@Scheduled(initialDelay = 10000, fixedDelay = 60000)
	public void submitWordlTimeSync() {
		try {
			service.getProcessInstances();
			// Listowanie zakonczonych procesow
			service.listCompletedProcesses();
			// Uruchamianie procesu testowego do wywolania w zdalnym konektorze inwokacji REST i zwrocenie wyniku do procesu
			//service.engageTimeRequest();
			service.taskAssignedListener();
			service.taskCompletedListener();
		}
		catch(Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}
}
