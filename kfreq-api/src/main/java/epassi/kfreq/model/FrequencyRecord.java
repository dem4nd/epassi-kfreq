package epassi.kfreq.model;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Word occurance count")
public record FrequencyRecord(String word, int count) { }
