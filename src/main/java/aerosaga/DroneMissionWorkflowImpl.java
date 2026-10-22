package aerosaga;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private final DroneMissionActivities activities =
            Workflow.newActivityStub(
                    DroneMissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setInitialInterval(Duration.ofSeconds(2))
                                            .setBackoffCoefficient(2.0)
                                            .setMaximumAttempts(3)
                                            .build()
                            )
                            .build()
            );

    @Override
    public String executeMission(String missionId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Starting drone mission: " + missionId);

        activities.takeOff(missionId);

        activities.travel(missionId);

        activities.returnHome(missionId);

        return "Mission completed: " + missionId;
    }
}