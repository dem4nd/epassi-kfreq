package epassi.kfreq.controller;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "kfreq")
public class Conf {

  private String defaultEncoding;

  private Integer minLength;

  private Integer maxLength;

  private List<String> stopWords;

  public List<String> getStopWords() {
    return stopWords;
  }

  public void setStopWords(List<String> stopWords) {
    this.stopWords = stopWords;
  }

  public String getDefaultEncoding() {
    return defaultEncoding;
  }

  public void setDefaultEncoding(String defaultEncoding) {
    this.defaultEncoding = defaultEncoding;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }
}