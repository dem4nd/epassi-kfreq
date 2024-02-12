package epassi.kfreq.model;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Service status report")
public record Status(
    int code,
    String description,
    @Schema(description = "Documents processed since service has been started")
    int processed) { }