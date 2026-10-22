
package aerosaga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import excecption.MissionNotFoundException;
import excecption.MissionPersistenceException;
public class DroneMissionActivitiesImpl implements DroneMissionActivities {

    private final ConcurrentMap<String, Integer> attempts =
            new ConcurrentHashMap<>();

    private void saveMission(
            String mission_id,
            String status) {

        String sql = """
            UPDATE missions
            SET status = ?,
                start_time = CASE
                    WHEN ? = 'RUNNING'
                    THEN COALESCE(start_time, CURRENT_TIMESTAMP)
                    ELSE start_time
                END,
                end_time = CASE
                    WHEN ? = 'COMPLETED'
                    THEN CURRENT_TIMESTAMP
                    ELSE end_time
                END
            WHERE mission_id = ?
            """;

        try (Connection connection =
                DatabaseConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)) {

   

       } catch (MissionNotFoundException e) {
           throw e;

       } catch (SQLException e) {
           throw new MissionPersistenceException(
               "Failed to update mission: " + mission_id,
               e
           );
       }
    }

    @Override
    public String takeOff(String mission_id) {

        System.out.println(
            "Drone taking off: " + mission_id
        );

        saveMission(mission_id, "RUNNING");

        return "TAKEOFF_SUCCESS";
    }

    @Override
    public String travel(String mission_id) {

        int attempt = attempts.merge(
            mission_id, 1, Integer::sum
        );

        System.out.println(
            "Drone travelling: " + mission_id
                + " | Attempt: " + attempt
        );

        if (attempt == 1) {
            System.out.println(
                "Simulated travel failure!"
            );

            throw new RuntimeException(
                "Simulated drone travel failure"
            );
        }

        System.out.println("Travel successful!");

        return "TRAVEL_SUCCESS";
    }

    @Override
    public String returnHome(String mission_id) {

        System.out.println(
            "Drone returning home: " + mission_id
        );

        saveMission(mission_id, "COMPLETED");

        return "RETURN_SUCCESS";
    }
}
