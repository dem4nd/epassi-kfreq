package epassi.kfreq.app;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class KFreqController {

  @GetMapping("/status")
  public Status status() {
    return new Status(0, "Ok");
  }
}
