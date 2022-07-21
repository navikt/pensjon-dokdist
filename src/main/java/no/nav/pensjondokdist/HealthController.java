package no.nav.pensjondokdist;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/api/internal/isReady")
    public String isReady() {
        return "ready";
    }

    @GetMapping("/api/internal/isAlive")
    public String isAlive() {
        return "alive";
    }
}
