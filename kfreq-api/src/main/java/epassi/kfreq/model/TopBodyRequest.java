package epassi.kfreq.model;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top words source request parameters")
public record TopBodyRequest(
    @Schema(description = "URL of source S3 object",
        example = "https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt")
    String resourceUrl,
    @Schema(description = "Most frequent words limit",
        example = "8")
    int limit,
    @Schema(description = "Encoding of source text (optional)",
        example = "CP1251",
        defaultValue = "UTF-8")
    String encoding) { }

