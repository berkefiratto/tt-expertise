package com.ttexpertise.repository;

import com.ttexpertise.model.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByAnswerIdIn(Collection<UUID> ids);

}
