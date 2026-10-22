package repository;

import entity.Mission;
import enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByStatus(MissionStatus status);

    List<Mission> findByDrone_Id(Long droneId);

    List<Mission> findByDrone_IdAndStatus(Long droneId, MissionStatus status);

    Mission findTopByDrone_IdOrderByCreatedAtDesc(Long droneId);

    List<Mission> findByStatusIn(List<MissionStatus> statuses);

    List<Mission> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Mission> findByDrone_IdAndCreatedAtBetween(
        Long droneId, LocalDateTime start, LocalDateTime end);
}
