package service;
import java.util.*;
import entity.Drone;
import entity.Mission;
import enums.DroneStatus;
import enums.MissionStatus;
import repository.DroneRepository;
import repository.MissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionService {

    private final DroneRepository droneRepository;
    private final MissionRepository missionRepository;
    private final DroneService droneService;

    public MissionService(
            DroneRepository droneRepository,
            MissionRepository missionRepository,
            DroneService droneService) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.droneService = droneService;
    }

    // Create a mission for an existing drone
    @Transactional
    public Mission createMission(
            Long droneId,
            double destinationLatitude,
            double destinationLongitude) {

        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Drone not found: " + droneId));

        validateCoordinates(destinationLatitude, destinationLongitude);

        if (drone.getStatus() != DroneStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Drone must be AVAILABLE to accept a mission");
        }

        Mission mission = new Mission(drone, destinationLatitude, destinationLongitude);
        mission.setStatus(MissionStatus.PENDING);

        return missionRepository.save(mission);
    }
    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Mission getMission(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Mission not found with ID: " + id));
    }

    // Execute the mission using the simplified drone simulator
    @Transactional
    public Mission executeMission(Long missionId) {
        Mission mission = getMission(missionId);
        Drone drone = mission.getDrone();

        if (mission.getStatus() != MissionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING missions can be executed");
        }

        mission.setStatus(MissionStatus.IN_PROGRESS);
        missionRepository.save(mission);

        droneService.takeoff(drone.getId());
        droneService.move(drone.getId(),
                mission.getDestinationLatitude(),
                mission.getDestinationLongitude());
        droneService.returnToHome(drone.getId());
        droneService.land(drone.getId());

        mission.setStatus(MissionStatus.COMPLETED);

        return missionRepository.save(mission);
    }

    // Mark a mission as failed
    @Transactional
    public Mission failMission(Long missionId) {
        Mission mission = getMission(missionId);

        if (mission.getStatus() != MissionStatus.PENDING
                && mission.getStatus() != MissionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only pending or in-progress missions can fail");
        }

        mission.setStatus(MissionStatus.FAILED);

        return missionRepository.save(mission);
    }
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Invalid destination coordinates");
        }
    }
}
