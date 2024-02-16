package epassi.kfreq.service;

import epassi.kfreq.model.FrequencyRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

@Component
public class KFreqCache {

  private final WeakHashMap<String, List<FrequencyRecord>> cache = new WeakHashMap<>();

  public Optional<List<FrequencyRecord>> get(String url, int limit) {
    return Optional.ofNullable(cache.get(url))
        .filter(list -> list.size() >= limit)
        .map(list -> list.size() == limit
            ? list
            : list.stream().limit(limit).toList());
  }

  public List<FrequencyRecord> put(String url, List<FrequencyRecord> winners) {
    cache.put(url, winners);
    return winners;
  }
}
