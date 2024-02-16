package epassi.kfreq.model;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Word occurance count")
public record FrequencyRecord(
    @Schema(description = "Counted word", example = "example")
    String word,
    @Schema(description = "The number of these words in the text", example = "3")
    int count) implements Comparable<FrequencyRecord> {

  @Override
  public int compareTo(FrequencyRecord o) {
    return o.count() - this.count();
  }
}

