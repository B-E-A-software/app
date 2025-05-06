package ro.unibuc.hello.controller;

import java.lang.Thread;
import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Counter;

@RestController
@RequestMapping("/quality")
public class TestAuthRegisterFailures {

    private Counter registerFailureCounter;

    public TestAuthRegisterFailures(Counter registerFailureCounter) {
        this.registerFailureCounter = registerFailureCounter;
    }

    @PostMapping("/down")
    public ResponseEntity<Void> markDown() {
        for (int i = 0; i < 1000; i++) {
            registerFailureCounter.increment();
        }
        return ResponseEntity.ok().build();
    }
}
