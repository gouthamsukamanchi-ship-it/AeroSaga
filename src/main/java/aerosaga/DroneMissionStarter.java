package aerosaga;

import aerosaga.DroneMissionWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class DroneMissionStarter {

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(service);

        DroneMissionWorkflow workflow =
                client.newWorkflowStub(
                        DroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue("DRONE_TASK_QUEUE")
                                .setWorkflowId("drone-mission-" + System.currentTimeMillis())
                                .build()
                );

        System.out.println("Starting mission 108...");

        try {
            String result = workflow.executeMission("108");

            System.out.println("Mission result: " + result);

        } finally {
            service.shutdown();
        }
    }
}