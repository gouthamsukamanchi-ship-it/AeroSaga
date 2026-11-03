package com.aerosaga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aerosaga.entity.SafetyEvent;

@Repository
public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {

    List<SafetyEvent> findByDroneIdOrderByCreatedAtDesc(Long droneId);

    List<SafetyEvent> findByMissionIdOrderByCreatedAtDesc(Long missionId);

    List<SafetyEvent> findBySeverity(String severity);

    List<SafetyEvent> findByResolved(Boolean resolved);

    List<SafetyEvent> findByWorkflowStatus(String workflowStatus);

    List<SafetyEvent> findAllByOrderByCreatedAtDesc();
}
