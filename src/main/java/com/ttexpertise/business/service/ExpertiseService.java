package com.ttexpertise.business.service;

import com.ttexpertise.model.dto.CreateExpertiseRequest;
import com.ttexpertise.model.dto.ReadExpertiseResponse;

import java.util.UUID;

public interface ExpertiseService {
    ReadExpertiseResponse readForCar(String carId);
    UUID create(CreateExpertiseRequest request);
}
