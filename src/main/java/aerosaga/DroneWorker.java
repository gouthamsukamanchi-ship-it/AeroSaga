package aerosaga;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
public class DroneWorker {

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(service);

        WorkerFactory factory =
                WorkerFactory.newInstance(client);

        Worker worker =
                factory.newWorker("DRONE_TASK_QUEUE");

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new DroneMissionActivitiesImpl()
        );

        factory.start();

        System.out.println("=================================");
        System.out.println("AeroSaga Drone Worker Started");
        System.out.println("Task Queue: DRONE_TASK_QUEUE");
        System.out.println("Waiting for missions...");
        System.out.println("=================================");

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        factory.shutdown();
        service.shutdown();
    }
}