
package service;

import entity.Drone;
import enums.DroneStatus;
import repository.DroneRepository;
import repository.TelemetryRepository;
import entity.Telemetry;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DroneService {

    private static final double TAKEOFF_BATTERY_COST = 5.0;
    private static final double MOVE_BATTERY_COST = 2.0;

    private final DroneRepository droneRepository;
    private final TelemetryRepository telemetryRepository;

    public DroneService(DroneRepository droneRepository,
                        TelemetryRepository telemetryRepository) {
        this.droneRepository = droneRepository;
        this.telemetryRepository = telemetryRepository;
    }

    @Transactional
    public Drone preflightCheck(Long droneId) {
        Drone drone = getDrone(droneId);
        Telemetry telemetry = telemetryRepository.findById(droneId).orElseThrow();
        if (drone.getStatus() != DroneStatus.AVAILABLE) {
            throw new IllegalStateException("Drone must be AVAILABLE before takeoff");
        }
        if (telemetry.getBatteryLevel() == null || drone.getBatteryLevel() < 30) {
            throw new IllegalStateException("Insufficient battery for takeoff");
        }
        return drone;
    }

    @Transactional
    public Drone takeoff(Long droneId) {
        Drone drone = preflightCheck(droneId);
        drone.setStatus(DroneStatus.IN_FLIGHT);
        drone.setBatteryLevel(Math.max(0, drone.getBatteryLevel() - TAKEOFF_BATTERY_COST));
        Drone saved = droneRepository.save(drone);
        logTelemetry(saved);
        return saved;
    }

    @Transactional
    public Drone move(Long droneId, double latitude, double longitude) {
        Drone drone = getDrone(droneId);
        Telemetry telemetry = telemetryRepository.findById(droneId).orElseThrow();
        if (drone.getStatus() != DroneStatus.IN_FLIGHT) {
            throw new IllegalStateException("Drone must be IN_FLIGHT to move");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid latitude or longitude");
        }
        if (telemetry.getBatteryLevel() == null || drone.getBatteryLevel() <= 10) {
            throw new IllegalStateException("Battery too low. Return the drone to base");
        }

        drone.setLatitude(latitude);
        drone.setLongitude(longitude);
        drone.setBatteryLevel(Math.max(0, drone.getBatteryLevel() - MOVE_BATTERY_COST));
        Drone saved = droneRepository.save(drone);
        logTelemetry(saved);
        return saved;
    }

    @Transactional
    public Drone returnToHome(Long droneId) {
        Drone drone = getDrone(droneId);
        Telemetry telemetry = telemetryRepository.findById(droneId).orElseThrow();
        if (drone.getStatus() != DroneStatus.IN_FLIGHT) {
            throw new IllegalStateException("Drone must be IN_FLIGHT to return home");
        }
        if (telemetry.getBatteryLevel() == null || drone.getBatteryLevel() <= 0) {
            throw new IllegalStateException("Battery depleted; simulated return cannot proceed");
        }

        drone.setLatitude(drone.getHomeLatitude());
        drone.setLongitude(drone.getHomeLongitude());
        drone.setStatus(DroneStatus.LANDING);
        Drone saved = droneRepository.save(drone);
        logTelemetry(saved);
        return saved;
    }

    @Transactional
    public Drone land(Long droneId) {
        Drone drone = getDrone(droneId);

        if (drone.getStatus() != DroneStatus.LANDING) {
            throw new IllegalStateException("Drone must be LANDING before landing completes");
        }

        drone.setStatus(DroneStatus.AVAILABLE);
        Drone saved = droneRepository.save(drone);
        logTelemetry(saved);
        return saved;
    }

    private Drone getDrone(Long droneId) {
        return droneRepository.findById(droneId)
                .orElseThrow(() -> new IllegalArgumentException("Drone not found: " + droneId));
    }

    private void logTelemetry(Drone drone) {
        Telemetry telemetry = new Telemetry();
        telemetry.setDroneId(drone.getId());
        telemetry.setBatteryLevel(drone.getBatteryLevel());
        telemetry.setLatitude(drone.getLatitude());
        telemetry.setLongitude(drone.getLongitude());
        telemetry.setRecordedAt(LocalDateTime.now());
        telemetryRepository.save(telemetry);
    }
}
