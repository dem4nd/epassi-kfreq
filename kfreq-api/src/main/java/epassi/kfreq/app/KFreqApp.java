package epassi.kfreq.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "epassi.kfreq")
public class KFreqApp {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(KFreqApp.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }
}
