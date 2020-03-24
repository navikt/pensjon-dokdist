package no.nav.pensjondokdist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthController {
    @GetMapping("/api/internal/isReady")
    @ResponseBody
    public String isReady() {
        return "ready";
    }

    @GetMapping("/api/internal/isAlive")
    @ResponseBody
    public String isAlive() {
        return "alive";
    }
}
