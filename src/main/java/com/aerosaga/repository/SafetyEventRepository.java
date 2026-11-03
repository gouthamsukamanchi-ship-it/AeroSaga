package com.aerosaga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aerosaga.entity.SafetyEvent;

public interface SafetyEventRepository
        extends JpaRepository<SafetyEvent, Long> {

    List<SafetyEvent> findByDroneIdOrderByCreatedAtDesc(Long droneId);

    List<SafetyEvent> findByMissionIdOrderByCreatedAtDesc(Long missionId);

    List<SafetyEvent> findAllByOrderByCreatedAtDesc();
}
