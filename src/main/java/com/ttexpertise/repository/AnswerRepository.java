package com.ttexpertise.repository;

import com.ttexpertise.model.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByExpertiseId(UUID expertiseId);

}
