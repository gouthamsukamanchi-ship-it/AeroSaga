package com.aerosaga;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import io.temporal.workflow.Workflow;

class MissionFailureHandlingTest {

    @Test
    void shouldMarkMissionFailedAfterRetryExhaustion() {

        AtomicInteger attempts = new AtomicInteger();
        AtomicBoolean failureRecorded = new AtomicBoolean(false);

        DroneMissionActivities activities = new DroneMissionActivities() {

            public String takeOff(String id) {
                return "TAKEOFF_SUCCESS";
            }

            public String travel(String id) {
                attempts.incrementAndGet();
                throw new RuntimeException("Permanent travel failure");
            }

            public String returnHome(String id) {
                return "RETURN_SUCCESS";
            }

            public String emergencyReturnHome(String id, String reason) {
                return "EMERGENCY_RETURN_SUCCESS";
            }

            public void failMission(String id) {
                failureRecorded.set(true);
            }
        };

        try (TestWorkflowEnvironment env =
                     TestWorkflowEnvironment.newInstance()) {

            Worker worker = env.newWorker("DRONE_TASK_QUEUE");

            worker.registerWorkflowImplementationTypes(
                    DroneMissionWorkflowImpl.class
            );

            worker.registerActivitiesImplementations(activities);

            env.start();

            DroneMissionWorkflow workflow =
                    env.getWorkflowClient().newWorkflowStub(
                            DroneMissionWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue("DRONE_TASK_QUEUE")
                                    .setWorkflowId("failure-test-1")
                                    .build()
                    );

            assertThrows(
                    io.temporal.client.WorkflowFailedException.class,
                    () -> workflow.executeMission("failure-test-1")
            );

            assertEquals(3, attempts.get());
            assertTrue(failureRecorded.get());
        }
    }
}