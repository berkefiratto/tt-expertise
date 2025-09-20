package com.ttexpertise.repository;

import com.ttexpertise.model.entity.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertiseRepository extends JpaRepository<Expertise, Long> {

    Optional<Expertise> findTopByCarIdOrderByCreatedAtDesc(String carId);

}
