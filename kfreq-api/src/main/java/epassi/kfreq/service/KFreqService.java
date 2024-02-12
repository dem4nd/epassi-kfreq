package epassi.kfreq.service;

import epassi.kfreq.dao.S3IOException;
import epassi.kfreq.dao.S3ObjectsDao;
import epassi.kfreq.model.FrequencyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


@Component
public class KFreqService {

  @Autowired
  private S3ObjectsDao dao;

  private final AtomicInteger processedCount = new AtomicInteger(0);

  private static class WordsDictionary {
    private int limit;

    private int minLength;

    private int maxLength;

    private Set<String> stopWords;

    private Map<String, Integer> words = new HashMap<String, Integer>();

//    private SortedSet<FrequencyRecord> winners = new TreeSet<FrequencyRecord>();

    public static final Pattern hyphenRe = Pattern.compile("(^-.+)|(.+-$)");

    public WordsDictionary(int limit, int minLength, int maxLength, Set<String> stopWords) {
      this.limit = limit;
      this.minLength = minLength;
      this.maxLength = maxLength;
      this.stopWords = stopWords;
    }

    // Returns Optional normalized word:
    //   - in lowercase if word meets requirements
    //   - empty if it doesn't.
    // Requirements:
    //   - lenght in range (minLength:maxLength)
    //   - hyphen '-' character is not in first or last position
    //   - the word is not in stop list specified in app config
    Optional<String> filterWord(String word) {
      if (word.length() >= minLength
          && word.length() <= maxLength
          && !hyphenRe.matcher(word).matches()) {
        return Optional.of(word.toLowerCase()).filter(w -> !stopWords.contains(w));
      }
      return Optional.empty();
    }

    void addWord(String word) {
      filterWord(word)
          .ifPresent(w -> words.put(w, words.getOrDefault(w, 0) + 1));
    }

    // Returns list of winners
    List<FrequencyRecord> result() {
      return words.entrySet().stream()
          .map(e -> new FrequencyRecord(e.getKey(), e.getValue()))
          .sorted()
          .limit(limit)
          .toList();
    }
  }

  public List<FrequencyRecord> runCompetionon(String url, int limit,
      String encoding, int minLength, int maxLength, Set<String> stopWords)
      throws S3IOException, IOException {

    try (InputStream ist = dao.getObjectInputStream(url)) {
      WordsDictionary wordsDic = new WordsDictionary(limit, minLength, maxLength, stopWords);

      Reader reader = new BufferedReader(new InputStreamReader(ist, encoding));
      int r;
      StringBuilder currentWord = new StringBuilder();

      while ((r = reader.read()) != -1) {
        char ch = (char) r;
        if (Character.isAlphabetic(ch) || ch == '-') {
          if (currentWord.length() <= maxLength) { // avoid
            currentWord.append(ch);
          }
        } else {
          wordsDic.addWord(currentWord.toString());
          currentWord.setLength(0);
        }
      }
      if (!currentWord.isEmpty()) {
        wordsDic.addWord(currentWord.toString());
      }

      processedCount.incrementAndGet();
      return wordsDic.result();
    } catch (UnsupportedEncodingException x) {
      throw new S3IOException("Illegal encoding: " + encoding);
    } catch (IOException x) {
      throw new S3IOException(x.getMessage());
    } catch (S3IOException x) {
      throw x;
    }
  }

  public int getProcessedCount() {
    return processedCount.get();
  }
}
