package ro.unibuc.hello.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.unibuc.hello.config.AvailabilityIndicator;

@RestController
@RequestMapping("/availability")
public class TestShutdownController {

    private final AvailabilityIndicator indicator;

    public TestShutdownController(AvailabilityIndicator indicator) {
        this.indicator = indicator;
    }

    /** Mark the service as down (availability = 0) */
    @PostMapping("/down")
    public ResponseEntity<Void> markDown() {
        indicator.setUp(0);
        return ResponseEntity.ok().build();
    }

    /** (Optionally) Mark the service back up (availability = 1) */
    @PostMapping("/up")
    public ResponseEntity<Void> markUp() {
        indicator.setUp(1);
        return ResponseEntity.ok().build();
    }
}
