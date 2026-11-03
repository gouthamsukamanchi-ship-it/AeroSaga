package aerosaga;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DroneMissionActivities {

    @ActivityMethod
    String takeOff(String missionId);

    @ActivityMethod
    String travel(String missionId);

    @ActivityMethod
    String returnHome(String missionId);

    String emergencyReturnHome(String missionId, String reason);

    void failMission(String missionId);
    
}