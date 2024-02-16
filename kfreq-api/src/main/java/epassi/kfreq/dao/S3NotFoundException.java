package epassi.kfreq.dao;


public class S3NotFoundException extends S3Exception {
  public S3NotFoundException(String message) {
    super(message);
  }
}
