package epassi.kfreq.dao;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

public class S3ObjectsDao {

  private record ObjectLocationParts(String regionName, String bucket, String objectKey) { }

  private static ObjectLocationParts parseObjectUrl(URL url) {
    return new ObjectLocationParts("", "", "");
  }

  private Optional<AmazonS3> s3client = Optional.empty();

  public static final AWSCredentials credentials = new BasicAWSCredentials(
      System.getenv("S3_ACCESS_KEY"),
      System.getenv("S3_SECRET"));

  private void actualizeClient(String region) {
    if (s3client.stream().noneMatch(c -> c.getRegionName().equals(region))) {
      s3client.ifPresent(AmazonS3::shutdown);
      s3client = Optional.empty();

      s3client = Optional.of(
          AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(region))
            .build()
      );
    }
  }

  public InputStream getObjectInputStream(URL url) {
//    ObjectLocationParts objParts = parseObjectUrl(url);
    ObjectLocationParts objParts = new ObjectLocationParts("eu-north-1", "dev.01", "epassi/steinbeck.txt");

    actualizeClient(objParts.regionName);

    // s3client is alwais present. If not, exception was thrown at foregoing actualizeClient
    return s3client.get()
        .getObject(objParts.bucket, objParts.objectKey)
        .getObjectContent();
  }
}
