
package com.AeroSaga.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AeroSaga.entity.Drone;
import com.AeroSaga.repository.DroneRepository;
import com.AeroSaga.exception.ResourceNotFoundException;

@Service
public class DroneService {

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    public List<Drone> getAllDrones() {
        return droneRepository.findAll();
    }

    public Drone getDroneById(Long id) {
        return droneRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Drone not found with id: " + id
                    )
                );
    }

    public Drone createDrone(Drone drone) {
        drone.setId(null);
        return droneRepository.save(drone);
    }
    
    public Drone updateDrone(Long id, Drone updatedDrone) {
        Drone existingDrone = getDroneById(id);

        existingDrone.setName(updatedDrone.getName());
        existingDrone.setModel(updatedDrone.getModel());
        existingDrone.setLocation(updatedDrone.getLocation());
        existingDrone.setStatus(updatedDrone.getStatus());

        return droneRepository.save(existingDrone);
    }
}
