
package com.AeroSaga.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.AeroSaga.entity.Drone;

public interface DroneRepository extends JpaRepository<Drone, Long> {

}
