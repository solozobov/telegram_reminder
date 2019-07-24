package com.solozobov.andrei;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.ApiContextInitializer;

/**
 * solozobov on 03/12/2018
 */

@PropertySource({"classpath:spring/application.properties"})
@ImportResource({"classpath:spring/domain.xml"})
@ComponentScan
public class Application {
  public static void main(String[] args) {
    ApiContextInitializer.init();
    SpringApplication.run(Application.class, args);
  }
}
