package epassi.kfreq.dao;


public class IllegalS3ClientException extends S3Exception {
  public IllegalS3ClientException(String message) {
    super(message);
  }
}
