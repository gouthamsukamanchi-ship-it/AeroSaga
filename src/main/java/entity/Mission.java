package entity;

import enums.MissionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL auto-increment
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @Column(nullable = false)
    private double destinationLatitude;

    @Column(nullable = false)
    private double destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status = MissionStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Mission() {
    }

    public Mission(
            Drone drone,
            double destinationLatitude,
            double destinationLongitude) {

        if (drone == null) {
            throw new IllegalArgumentException("Drone is required");
        }

        if (!Double.isFinite(destinationLatitude)
                || destinationLatitude < -90
                || destinationLatitude > 90
                || !Double.isFinite(destinationLongitude)
                || destinationLongitude < -180
                || destinationLongitude > 180) {
            throw new IllegalArgumentException(
                    "Invalid destination coordinates");
        }

        this.drone = drone;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.status = MissionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Drone getDrone() {
        return drone;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setStatus(MissionStatus status) {
        this.status = status;
    }
}
