package epassi.kfreq.controller;

import epassi.kfreq.dao.S3IOException;
import epassi.kfreq.model.FrequencyRecord;
import epassi.kfreq.model.Status;
import epassi.kfreq.model.TopBodyRequest;
import epassi.kfreq.service.KFreqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


//@Configuration
//@ConfigurationProperties(prefix = "kfreq")
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Epassi Coding Test", description = "REST API for finding the top K most frequent words in file")
public class KFreqController {

  @Autowired
  private KFreqService engine;

  @Autowired
  private Conf conf;

  private Set<String> stopWords;

  @PostConstruct
  private void init() {
    stopWords = conf.getStopWords().stream().map(String::toLowerCase).collect(Collectors.toSet());
  }

  @GetMapping(value = "/status", produces = "application/json")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(
              implementation = Status.class), mediaType = "application/json"))
  })
  @Operation(
      summary = "Get API server status and statistics")
  public ResponseEntity<Status> status() {
    return ResponseEntity.ok(new Status(0, "Ok", engine.getProcessedCount()));
  }

  @PostMapping(value = "/top", consumes = "application/json", produces = "application/json")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(array = @ArraySchema(
              schema = @Schema(implementation = FrequencyRecord.class)), mediaType = "application/json")),
      @ApiResponse(responseCode = "404",
          description = "The object with given url was not found.",
          content = @Content(schema = @Schema()))
  })
  @Operation(
      summary = "",
      description = "")
  public ResponseEntity<List<FrequencyRecord>> topCount(@RequestBody TopBodyRequest top)
      throws ResponseStatusException {

    String actualEncoding = Optional.ofNullable(top.getEncoding()).orElse(conf.getDefaultEncoding());
    Set<String> actualStopWords = top.isUseStopWords() ? stopWords : Collections.emptySet();

    try {
      return ResponseEntity.ok(engine.runCompetionon(top.getResourceUrl(), top.getLimit(),
          actualEncoding, conf.getMinLength(), conf.getMaxLength(), actualStopWords));
    } catch (S3IOException x) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, x.getMessage());
    } catch (IOException x) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, x.getMessage());
    }
  }
}
