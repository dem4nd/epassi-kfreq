package epassi.kfreq.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "epassi.kfreq")
public class KFreqApp {

  public static void main(String[] args) {
    SpringApplication.run(KFreqApp.class, args);
  }
}
