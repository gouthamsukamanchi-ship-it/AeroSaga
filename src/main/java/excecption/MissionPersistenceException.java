
package excecption;

public class MissionPersistenceException extends RuntimeException {

    public MissionPersistenceException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}
