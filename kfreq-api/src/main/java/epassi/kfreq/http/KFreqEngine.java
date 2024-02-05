package epassi.kfreq.http;

import epassi.kfreq.model.FrequencyRecord;
import epassi.kfreq.model.TopBodyRequest;

import java.util.Arrays;
import java.util.List;

public class KFreqEngine {

  public List<FrequencyRecord> gather(String url, int limit, String encoding) {
    return Arrays.asList(
        new FrequencyRecord("hello", 7),
        new FrequencyRecord("boy", 94),
        new FrequencyRecord("regular", 14)
    );
  }
}
