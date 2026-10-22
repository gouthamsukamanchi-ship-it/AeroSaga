
package service;

import entity.Drone;
import entity.Telemetry;
import repository.TelemetryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @Transactional
    public Telemetry recordTelemetry(Drone drone, Double altitude) {

        Telemetry telemetry = new Telemetry();

        telemetry.setDroneId(drone.getId());
        telemetry.setBatteryLevel(drone.getBatteryLevel());
        telemetry.setLatitude(drone.getLatitude());
        telemetry.setLongitude(drone.getLongitude());
        telemetry.setAltitude(altitude);

        return telemetryRepository.save(telemetry);
    }

    public List<Telemetry> getTelemetryByDrone(Long droneId) {
        return telemetryRepository.findByDroneId(droneId);
    }

    public List<Telemetry> getAllTelemetry() {
        return telemetryRepository.findAll();
    }
}
