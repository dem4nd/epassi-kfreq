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
  public static final String S3_CREDENTIALS_ENV_PROP = "S3_CREDENTIALS_FILE";
  public static final String S3_ACCESS_KEY_PROP = "S3_ACCESS_KEY";
  public static final String S3_SECRET_PROP = "S3_SECRET";

  private final Logger logger = LoggerFactory.getLogger(S3ObjectsDao.class);

  @Autowired
  private Environment env;

  private Optional<AmazonS3> s3Client = Optional.empty();

  private final S3Utilities s3Utilities = S3Client.create().utilities();

  private Optional<AWSCredentials> credentials = Optional.empty();

  private record ObjectLocationParts(String regionName, String bucket, String objectKey) { }

  // Fills credentials object property or leaves it empty.
  // Empty credentials leave empty s3Client property after actualizeClient
  // and raise error exception in getObjectInputStream.
  @PostConstruct
  public void readCredentials () {
    Optional<String> accessKey = Optional.ofNullable(System.getenv(S3_ACCESS_KEY_PROP));
    Optional<String> secret = Optional.ofNullable(System.getenv(S3_SECRET_PROP));

    if (accessKey.isEmpty() || secret.isEmpty()) {
      var credFileNameOpt = Optional.ofNullable(System.getenv(S3_CREDENTIALS_ENV_PROP))
          .or(() -> Optional.ofNullable(env.getProperty(S3_CREDENTIALS_PROP)));

      if (credFileNameOpt.isEmpty()) {
        logger.error("S3 credentials properties file is not specified in config or in environment variable");
      } else {
        var credFileName = credFileNameOpt.get();
        try {
          var propInp = new FileInputStream(credFileName);
          Properties prop = new Properties();
          prop.load(propInp);

          accessKey = accessKey.or(() -> Optional.ofNullable(prop.getProperty(S3_ACCESS_KEY_PROP)));
          if (accessKey.isEmpty()) {
            logger.error("S3 access key is not defined in credentials file or in environment variable");
          }
          secret = secret.or(() -> Optional.ofNullable(prop.getProperty(S3_SECRET_PROP)));
          if (secret.isEmpty()) {
            logger.error("S3 secret is not defined in credentials file or in environment variable");
          }
        } catch (FileNotFoundException x) {
          logger.error("S3 credentials properties file is not found: " + credFileName);
        } catch (IOException x) {
          logger.error("Error on reading S3 credentials properties: " + credFileName);
        } catch (IllegalArgumentException x) {
          logger.error(x.getMessage());
        }
      }
    }

    if (accessKey.isPresent() && secret.isPresent()) {
      credentials = Optional.of(new BasicAWSCredentials(
          accessKey.get(), secret.get()));
    }
  }

  private ObjectLocationParts parseObjectUrl(String url)
      throws S3ParsingUrlException {

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
        throw new S3ParsingUrlException(msg);
      }
    } catch (IllegalArgumentException x) {
      logger.error(x.getMessage());
      throw new S3ParsingUrlException(x.getMessage());
    }
  }

  private void actualizeClient(String region)
      throws S3IllegalClientException {

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
        throw new S3IllegalClientException(msg);
      }
    }
  }

  public InputStream getObjectInputStream(String url)
      throws S3Exception {

    try {
      var objParts = parseObjectUrl(url); // can throw S3ParsingUrlException
      actualizeClient(objParts.regionName); // can throw S3IllegalClientException
      return s3Client.get().getObject(objParts.bucket, objParts.objectKey).getObjectContent();
    } catch (SdkClientException x) {
      throw new S3NotFoundException(x.getMessage());
    }
  }
}
