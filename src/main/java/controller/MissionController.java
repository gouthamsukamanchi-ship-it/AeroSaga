package controller;

import entity.Mission;
import service.MissionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*")
public class MissionController {

    private final MissionService missionService;
    @GetMapping("/test")
    public String test() {
        return "MissionController is working!";
    }
    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // Get all missions
    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(
                missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                missionService.getMission(id));
    }

    // Create a new mission (no name field in entity)
    @PostMapping
    public ResponseEntity<Mission> createMission(
            @RequestParam Long droneId,
            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude) {

        Mission mission = missionService.createMission(
                droneId,
                destinationLatitude,
                destinationLongitude
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mission);
    }

    // Execute a mission
    @PostMapping("/{id}/execute")
    public ResponseEntity<Mission> executeMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.executeMission(id));
    }

    // Mark a mission as failed
    @PostMapping("/{id}/fail")
    public ResponseEntity<Mission> failMission(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.failMission(id));
    }
}
