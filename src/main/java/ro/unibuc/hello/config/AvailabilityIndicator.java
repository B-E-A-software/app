package ro.unibuc.hello.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
@Component
public class AvailabilityIndicator {
    private final AtomicInteger up = new AtomicInteger(1);
    public void setUp(int value) { up.set(value); }
    public int  getUp()         { return up.get();  }
}

