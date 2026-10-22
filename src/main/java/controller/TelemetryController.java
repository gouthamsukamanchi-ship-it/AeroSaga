
package controller;

import service.TelemetryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping
    public Object getAllTelemetry() {
        return telemetryService.getAllTelemetry();
    }

    @GetMapping("/drone/{droneId}")
    public Object getTelemetryByDrone(@PathVariable Long droneId) {
        return telemetryService.getTelemetryByDrone(droneId);
    }
}
