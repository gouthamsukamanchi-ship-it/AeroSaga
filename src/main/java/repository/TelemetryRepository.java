package repository;

import entity.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

    List<Telemetry> findByDroneId(Long droneId);

    List<Telemetry> findByDroneIdOrderByRecordedAtDesc(Long droneId);

    Telemetry findTopByDroneIdOrderByRecordedAtDesc(Long droneId);

    List<Telemetry> findByDroneIdAndRecordedAtBetween(
        Long droneId, LocalDateTime start, LocalDateTime end);

    List<Telemetry> findByBatteryLevelLessThan(Double threshold);

    List<Telemetry> findByLatitudeBetweenAndLongitudeBetween(
        Double latMin, Double latMax, Double lonMin, Double lonMax);
}
