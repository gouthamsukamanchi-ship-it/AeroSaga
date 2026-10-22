
package aerosaga;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BatchMissionStarter {

    private static final String TASK_QUEUE = "DRONE_TASK_QUEUE";

    private static List<Integer> fetchMissionIds() throws SQLException {
        List<Integer> missionIds = new ArrayList<>();

        String sql = """
            SELECT mission_id
            FROM missions
            ORDER BY mission_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                missionIds.add(rs.getInt("mission_id"));
            }
        }

        return missionIds;
    }

    public static void main(String[] args) throws Exception {

        List<Integer> missionIds = fetchMissionIds();

        System.out.println("Total missions found: " + missionIds.size());

        if (missionIds.isEmpty()) {
            System.out.println("No missions found.");
            return;
        }

        int succeeded = 0;
        int failed = 0;

        // Unique ID for this batch execution.
        String batchId = UUID.randomUUID().toString();

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(service);
            for (int missionId : missionIds) {
                System.out.println(
                    "\nStarting mission " + missionId + "..."
                );

                try {
                    DroneMissionWorkflow workflow =
                        client.newWorkflowStub(
                            DroneMissionWorkflow.class,
                            WorkflowOptions.newBuilder()
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowId(
                                    "drone-mission-" + missionId
                                    + "-" + batchId
                                )
                                .build()
                        );

                    String result =
                        workflow.executeMission(
                            String.valueOf(missionId)
                        );

                    System.out.println(
                        "Mission " + missionId + ": " + result
                    );

                    succeeded++;

                } catch (Exception e) {
                    System.err.println(
                        "Mission " + missionId + " FAILED: "
                        + e.getMessage()
                    );

                    failed++;
                }
            }
        System.out.println("\n========== BATCH SUMMARY ==========");
        System.out.println("Total missions: " + missionIds.size());
        System.out.println("Successful: " + succeeded);
        System.out.println("Failed: " + failed);
        System.out.println(
            "Accounted for: " + (succeeded + failed)
        );
        System.out.println("===================================");
    }
}

