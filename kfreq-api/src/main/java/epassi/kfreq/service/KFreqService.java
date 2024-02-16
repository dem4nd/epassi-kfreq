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

  @Autowired
  private KFreqCache cache;

  // Quantity of successfully processed requests - just for simple statistics in status request
  private final AtomicInteger processedCount = new AtomicInteger(0);

  private static class WordsDictionary {
    private int limit;

    private int minLength;

    private int maxLength;

    private Set<String> stopWords;

    private Map<String, Integer> words = new HashMap<String, Integer>();

    public static final Pattern hyphenRe = Pattern.compile("(^-.*)|(.+-$)");

    private WordsDictionary(int limit, int minLength, int maxLength, Set<String> stopWords) {
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
    private Optional<String> filterWord(String word) {
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

    // Returns list of winners (with limit condition)
    List<FrequencyRecord> result() {
      return words.entrySet().stream()
          .map(e -> new FrequencyRecord(e.getKey(), e.getValue()))
          .sorted()
          .limit(limit)
          .toList();
    }

    // for unit tests
    void clear() {
      words.clear();
    }
  }

  public List<FrequencyRecord> runCompetition(String url, int limit,
      String encoding, int minLength, int maxLength, Set<String> stopWords)
      throws S3IOException, KFreqUnsupportedEncodingException {

    var winners = cache.get(url, limit)
        .orElseGet(() -> {
          try (Reader reader = new BufferedReader(
              new InputStreamReader(
                  dao.getObjectInputStream(url), encoding))) {
            WordsDictionary wordsDic = new WordsDictionary(limit, minLength, maxLength, stopWords);

            int r;
            StringBuilder currentWord = new StringBuilder();

            while ((r = reader.read()) != -1) {
              char ch = (char) r;
              if (Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '-') {
                if (currentWord.length() <= maxLength) {
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

            return cache.put(url, wordsDic.result());
          } catch (UnsupportedEncodingException x) {
            throw new KFreqUnsupportedEncodingException("Illegal encoding: " + encoding);
          } catch (IOException x) {
            throw new S3IOException(x.getMessage());
          }
    });

    processedCount.incrementAndGet(); // incremented in success only
    return winners;
  }

  public int getProcessedCount() {
    return processedCount.get();
  }
}
