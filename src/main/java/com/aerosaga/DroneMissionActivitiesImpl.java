package com.aerosaga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.aerosaga.service.MissionService;

@Component
public class DroneMissionActivitiesImpl implements DroneMissionActivities {

    private final ConcurrentMap<String, Integer> attempts =
            new ConcurrentHashMap<>();
    private final MissionService missionService;

    public DroneMissionActivitiesImpl(MissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public String takeOff(String missionId) {
        System.out.println("Drone taking off: " + missionId);
        missionService.updateWorkflowStatus(missionId, "RUNNING");
        recordSignal(missionId, "TAKEOFF", "RUNNING", "Mission workflow started");

        return "TAKEOFF_SUCCESS";
    }

    @Override
    public String travel(String missionId) {

        int attempt = attempts.merge(missionId, 1, Integer::sum);

        System.out.println(
                "Drone travelling: " + missionId +
                " | Attempt: " + attempt
        );

        // Simulate a temporary/transient failure on first attempt
        if (attempt == 1) {

            System.out.println("❌ Simulated travel failure!");

            recordSignal(
                    missionId,
                    "TRAVEL",
                    "RETRYING",
                    "Temporary travel failure; retrying"
            );
            throw new RuntimeException(
                    "Simulated drone travel failure"
            );
        }

        recordSignal(missionId, "TRAVEL", "COMPLETED", "Travel completed");
        System.out.println("✅ Travel successful!");

        return "TRAVEL_SUCCESS";
    }

    @Override
    public String returnHome(String missionId) {
        System.out.println(
                "Drone returning home: " + missionId
        );
        missionService.updateWorkflowStatus(missionId, "COMPLETED");
        recordSignal(missionId, "RETURN_HOME", "COMPLETED", "Mission completed");

        return "RETURN_SUCCESS";
    }

    @Override
    public void failMission(String missionId) {
        missionService.updateWorkflowStatus(missionId, "FAILED");
        recordSignal(missionId, "MISSION", "FAILED", "Mission workflow failed");
    }

    private void recordSignal(
            String missionId,
            String type,
            String status,
            String message) {
        missionService.recordWorkflowSignal(missionId, type, status, message);
    }
}