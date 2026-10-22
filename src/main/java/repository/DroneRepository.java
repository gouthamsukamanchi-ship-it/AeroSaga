package repository;

import entity.Drone;
import enums.DroneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drone, Long> {

    Optional<Drone> findBySerialNumber(String serialNumber);

    List<Drone> findByStatus(DroneStatus status);

    List<Drone> findByBatteryLevelGreaterThanEqual(double minimumBattery);

    boolean existsBySerialNumber(String serialNumber);

    List<Drone> findByOrderByBatteryLevelDesc();

    List<Drone> findByStatusAndBatteryLevelGreaterThanEqual(
        DroneStatus status, double minimumBattery);

    List<Drone> findByLatitudeBetweenAndLongitudeBetween(
        double latMin, double latMax, double lonMin, double lonMax);

    List<Drone> findBySerialNumberContaining(String partialSerial);
}
