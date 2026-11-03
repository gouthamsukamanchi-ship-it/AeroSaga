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

        System.out.println("Sentinel: monitoring drone telemetry during travel...");

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Travel interrupted", e);
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
    public String emergencyReturnHome(String missionId, String reason) {
        System.out.println("EMERGENCY RETURN initiated: " + missionId);
        System.out.println("Reason: " + reason);

        missionService.updateWorkflowStatus(missionId, "EMERGENCY_RETURNED");
        recordSignal(
                missionId,
                "EMERGENCY_RETURN",
                "COMPLETED",
                "Emergency return completed. Reason: " + reason
        );

        return "EMERGENCY_RETURN_SUCCESS";
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