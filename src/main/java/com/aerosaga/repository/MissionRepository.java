package com.aerosaga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aerosaga.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByStatus(String status);

}