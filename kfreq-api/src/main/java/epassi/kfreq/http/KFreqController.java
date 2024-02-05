package epassi.kfreq.http;

import epassi.kfreq.model.FrequencyRecord;
import epassi.kfreq.model.Status;
import epassi.kfreq.model.TopBodyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class KFreqController {

  private static String CONF_KEY_ENCODING = "kfreq.default-encoding";
  private static String DEFAULT_ENCODING = "UTF-8";

  @Autowired
  private Environment env;

  private KFreqEngine engine = new KFreqEngine();

  @GetMapping("/status")
  public Status status() {
    return new Status(0, "Ok");
  }

  @PostMapping(value = "/top", consumes = "application/json", produces = "application/json")
  public List<FrequencyRecord> topCount(@RequestBody TopBodyRequest top) {
    String encoding = Optional.ofNullable(top.encoding())
        .orElse(env.getProperty(CONF_KEY_ENCODING, DEFAULT_ENCODING));

    return engine.gather(top.resourceUrl(), top.limit(), encoding);
  }

}
