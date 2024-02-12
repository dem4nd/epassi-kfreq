package epassi.kfreq.dao;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;


@Component
public class S3ObjectsDao {

  public static final String S3_CREDENTIALS_PROP = "s3.credentialsFile";
  public static final String S3_ACCESS_KEY_PROP = "access-key";
  public static final String S3_SECRET_PROP = "secret";

  private final Logger logger = LoggerFactory.getLogger(S3ObjectsDao.class);

  @Autowired
  private Environment env;

  private Optional<AmazonS3> s3Client = Optional.empty();

  private final S3Utilities s3Utilities = S3Client.create().utilities();

  private Optional<AWSCredentials> credentials = Optional.empty();

  private record ObjectLocationParts(String regionName, String bucket, String objectKey) { }

  // Fills credentials object property or leaves it empty.
  // Empty credentials leaves empty s3Client property after actualizeClient
  // and will raise error exception in getObjectInputStream.
  @PostConstruct
  public void readCredentials () {
    var credFileName = env.getProperty(S3_CREDENTIALS_PROP);
    if (credFileName == null) {
      logger.error("S3 credentials properties file is not specified in config");
    } else {
      try {
        var propInp = new FileInputStream(credFileName);
        Properties prop = new Properties();
        prop.load(propInp);
        credentials = Optional.of(new BasicAWSCredentials(
            prop.getProperty(S3_ACCESS_KEY_PROP),
            prop.getProperty(S3_SECRET_PROP)));
      } catch (FileNotFoundException x) {
        logger.error("S3 credentials properties file is not found: " + credFileName);
      } catch (IOException x) {
        logger.error("Error on reading S3 credentials properties: " + credFileName);
      } catch (IllegalArgumentException x) {
        logger.error(x.getMessage());
      }
    }
  }

  private ObjectLocationParts parseObjectUrl(String url)
      throws ParsingS3UrlException {

    try {
      S3Uri s3Uri = s3Utilities.parseUri(URI.create(url));
      var objParts = s3Uri.bucket()
          .flatMap(bucket -> s3Uri.key()
              .flatMap(key -> s3Uri.region()
                  .flatMap(region ->
                      Optional.of(new ObjectLocationParts(region.id(), bucket, key)))));
      if (objParts.isPresent()) {
        return objParts.get();
      } else {
        String msg = "URL is not in S3 object format: " + url;
        logger.error(msg);
        throw new ParsingS3UrlException(msg);
      }
    } catch (IllegalArgumentException x) {
      logger.error(x.getMessage());
      throw new ParsingS3UrlException(x.getMessage());
    }
  }

  private void actualizeClient(String region)
      throws IllegalS3ClientException {

    if (s3Client.stream().noneMatch(c -> c.getRegionName().equals(region))) {
      s3Client.ifPresent(AmazonS3::shutdown);
      s3Client = Optional.empty();

      try {
        s3Client = credentials.map(cr ->
            AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(cr))
                .withRegion(Regions.fromName(region))
                .build());
      } catch (IllegalArgumentException x) {
        String msg = String.format("Can not create client for region '%s'", region);
        logger.error(msg);
        throw new IllegalS3ClientException(msg);
      }
    }
  }

  public InputStream getObjectInputStream(String url)
      throws S3IOException {

    try {
      var objParts = parseObjectUrl(url); // can throw ParseS3URLException
      actualizeClient(objParts.regionName); // can throw IllegalS3ClientException
      return s3Client.get().getObject(objParts.bucket, objParts.objectKey).getObjectContent();
    } catch (S3Exception x) {
      throw new S3IOException(x.getMessage());
    }
  }
}
