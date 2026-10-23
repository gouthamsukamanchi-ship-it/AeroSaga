
package com.aerosaga.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aerosaga.dto.MissionRequest;
import com.aerosaga.entity.Drone;
import com.aerosaga.entity.Mission;
import com.aerosaga.exception.ResourceNotFoundException;
import com.aerosaga.repository.DroneRepository;
import com.aerosaga.repository.MissionRepository;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionService(
            MissionRepository missionRepository,
            DroneRepository droneRepository) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Mission getMissionById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Mission not found with id: " + id
                    )
                );
    }

    public Mission createMission(MissionRequest request) {
        Drone drone = droneRepository.findById(request.getDroneId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Drone not found with id: " + request.getDroneId()
                    )
                );

        if (!"AVAILABLE".equalsIgnoreCase(drone.getStatus())) {
            throw new IllegalStateException(
                "Drone is not available for a mission"
            );
        }

        Mission mission = new Mission();
        mission.setMissionName(request.getMissionName());
        mission.setDestination(request.getDestination());
        mission.setStatus("PENDING");
        mission.setDrone(drone);
        drone.setStatus("IN_PROGRESS");
        droneRepository.save(drone);

        return missionRepository.save(mission);
    }

    public List<Mission> getMissionsByStatus(String status) {
        return missionRepository.findByStatus(status);
    }

    public Mission updateMission(Long id, Mission updatedMission) {
        Mission existingMission = getMissionById(id);

        existingMission.setMissionName(updatedMission.getMissionName());
        existingMission.setDestination(updatedMission.getDestination());

        if (updatedMission.getStatus() != null
                && !updatedMission.getStatus().isBlank()) {
            existingMission.setStatus(updatedMission.getStatus());
        }

        return missionRepository.save(existingMission);
    }

    public void deleteMission(Long id) {
        if (!missionRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Mission not found with id: " + id
            );
        }

        missionRepository.deleteById(id);
    }
}
