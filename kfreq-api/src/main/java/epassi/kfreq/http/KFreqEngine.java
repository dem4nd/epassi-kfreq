package epassi.kfreq.http;

import epassi.kfreq.dao.S3ObjectsDao;
import epassi.kfreq.model.FrequencyRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KFreqEngine {

  private final S3ObjectsDao dao = new S3ObjectsDao();

  public List<FrequencyRecord> gather(String url, int limit, String encoding)
      throws MalformedURLException, UnsupportedEncodingException {

    var ist = dao.getObjectInputStream(new URL(url));

    String text = new BufferedReader(
        new InputStreamReader(ist, encoding))
        .lines()
        .collect(Collectors.joining("\n"));

    System.out.printf(text);

    return Arrays.asList(
        new FrequencyRecord("hello", 7),
        new FrequencyRecord("boy", 94),
        new FrequencyRecord("regular", 14)
    );
  }
}
