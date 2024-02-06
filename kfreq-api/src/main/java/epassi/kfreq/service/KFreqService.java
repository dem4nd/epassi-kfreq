package epassi.kfreq.service;

import epassi.kfreq.dao.S3ObjectsDao;
import epassi.kfreq.model.FrequencyRecord;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class KFreqService {

  private final S3ObjectsDao dao = new S3ObjectsDao();

  public List<FrequencyRecord> gather(String url, int limit, String encoding)
      throws MalformedURLException, UnsupportedEncodingException {
    var ist = dao.getObjectInputStream(new URL(url));
    return countWords(ist, limit, encoding);
  }

  public List<FrequencyRecord> countWords(InputStream ist, int limit, String encoding)
      throws UnsupportedEncodingException {

    Map<String, Integer> words = new HashMap<String, Integer>();

    try {
      Reader reader = new BufferedReader(new InputStreamReader(ist, encoding));
      int r;
      StringBuilder currentWord = new StringBuilder();
      while ((r = reader.read()) != -1) {
        char ch = (char) r;
        if (Character.isAlphabetic(ch)) {
          currentWord.append(ch);
        } else if (!currentWord.isEmpty()) {
          String word = currentWord.toString().toLowerCase();
          Integer count = words.get(word);
          if (count == null) {
            count = 1;
          } else {
            count = count + 1;
          }
          words.put(word, count);
          currentWord.setLength(0);
        }
      }
      if (!currentWord.isEmpty()) {
        String word = currentWord.toString().toLowerCase();
        Integer count = words.get(word);
        if (count == null) {
          count = 1;
        } else {
          count = count + 1;
        }
        words.put(word, count);
      }
    } catch (IOException x) {
      //
    }

    List<FrequencyRecord> recs = words.entrySet().stream()
        .map(e -> new FrequencyRecord(e.getKey(), e.getValue()))
        .sorted()
        .limit(limit)
        .toList();

    return recs;
  }
}
