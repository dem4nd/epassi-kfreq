package epassi.kfreq.model;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Service status report")
public record Status(int code, String description) { }