package com.aerosaga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DroneMissionActivitiesImpl implements DroneMissionActivities {

    private final ConcurrentMap<String, Integer> attempts =
            new ConcurrentHashMap<>();

    @Override
    public String takeOff(String missionId) {

        System.out.println("Drone taking off: " + missionId);

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

            throw new RuntimeException(
                    "Simulated drone travel failure"
            );
        }

        System.out.println("✅ Travel successful!");

        return "TRAVEL_SUCCESS";
    }

    @Override
    public String returnHome(String missionId) {

        System.out.println(
                "Drone returning home: " + missionId
        );

        return "RETURN_SUCCESS";
    }
}