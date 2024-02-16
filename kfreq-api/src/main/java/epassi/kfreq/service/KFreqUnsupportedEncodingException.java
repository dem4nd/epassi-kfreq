package epassi.kfreq.service;


import epassi.kfreq.dao.S3Exception;

public class KFreqUnsupportedEncodingException extends RuntimeException {
  public KFreqUnsupportedEncodingException(String message) {
    super(message);
  }
}
