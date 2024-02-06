package epassi.kfreq.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Epassi Coding Test", description = "REST API for finding the top K most frequent words in file")
public class KFreqController {

  private static String CONF_KEY_ENCODING = "kfreq.default-encoding";
  private static String DEFAULT_ENCODING = "UTF-8";

  @Autowired
  private Environment env;

  private final KFreqService engine = new KFreqService();

  @GetMapping(value = "/status", produces = "application/json")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(
              implementation = Status.class), mediaType = "application/json"))
  })
  @Operation(
      summary = "Get API server status and statistics")
  public ResponseEntity<Status> status() {
    return ResponseEntity.ok(new Status(0, "Ok"));
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
      summary = "Retrieve a Tutorial by Id",
      description = "Get a Tutorial object by specifying its id. The response is Tutorial object with id, title, description and published status.")
  public ResponseEntity<List<FrequencyRecord>> topCount(@RequestBody TopBodyRequest top)
      throws Exception {
    String encoding = Optional.ofNullable(top.encoding())
        .orElse(env.getProperty(CONF_KEY_ENCODING, DEFAULT_ENCODING));

    List<FrequencyRecord> result = engine.gather(top.resourceUrl(), top.limit(), encoding);

    return ResponseEntity.ok(result);
  }

}
