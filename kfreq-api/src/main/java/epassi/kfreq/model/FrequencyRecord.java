package epassi.kfreq.model;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Word occurance count")
public record FrequencyRecord(
    @Schema(description = "Counted word")
    String word,
    @Schema(description = "The number of these words in the text")
    int count) implements Comparable<FrequencyRecord> {

  @Override
  public int compareTo(FrequencyRecord o) {
    return o.count() - this.count();
  }
}

