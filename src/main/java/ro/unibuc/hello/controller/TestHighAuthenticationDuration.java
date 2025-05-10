package ro.unibuc.hello.controller;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/performance")
public class TestHighAuthenticationDuration {

    private Timer authenticationTimer;

    public TestHighAuthenticationDuration(Timer authenticationTimer) {
        this.authenticationTimer = authenticationTimer;
    }

    @PostMapping("/down")
    public ResponseEntity<Void> markDown() {
        for (int i = 0; i < 1000; i++)
        {
            authenticationTimer.record(Duration.ofSeconds(6));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/up")
    public ResponseEntity<Void> markUp() {
        for (int i = 0; i < 1000; i++)
        {
            authenticationTimer.record(Duration.ofSeconds(2));
        }
        return ResponseEntity.ok().build();
    }
}
