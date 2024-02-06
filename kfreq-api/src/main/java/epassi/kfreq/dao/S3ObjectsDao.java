package epassi.kfreq.dao;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class S3ObjectsDao {

  private Optional<AmazonS3> s3Client = Optional.empty();

  private final S3Utilities s3Utilities = S3Client.create().utilities();

  private record ObjectLocationParts(String regionName, String bucket, String objectKey) { }

  private Optional<ObjectLocationParts> parseObjectUrl(URL url) {
    Optional<ObjectLocationParts> result = Optional.empty();
    try {
      S3Uri s3Uri = s3Utilities.parseUri(url.toURI());
      result = s3Uri.bucket()
          .flatMap(bucket -> s3Uri.key()
              .flatMap(key -> s3Uri.region()
                  .flatMap(region ->
                      Optional.of(new ObjectLocationParts(region.id(), bucket, key)))));
    } catch (URISyntaxException x) {
      // leave result empty on error
    }
    return result;
  }

  public static final AWSCredentials credentials = new BasicAWSCredentials(
      System.getenv("S3_ACCESS_KEY"),
      System.getenv("S3_SECRET"));

  private void actualizeClient(String region) {
    if (s3Client.stream().noneMatch(c -> c.getRegionName().equals(region))) {
      s3Client.ifPresent(AmazonS3::shutdown);
      s3Client = Optional.empty();

      s3Client = Optional.of(
          AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(region))
            .build()
      );
    }
  }

  public InputStream getObjectInputStream(URL url) {
    var objParts = parseObjectUrl(url);
//    ObjectLocationParts objParts = new ObjectLocationParts("eu-north-1", "dev.01", "epassi/steinbeck.txt");

    actualizeClient(objParts.get().regionName);

    // s3client is alwais present. If not, exception was thrown at foregoing actualizeClient
    return s3Client.get()
        .getObject(objParts.get().bucket, objParts.get().objectKey)
        .getObjectContent();
  }
}
