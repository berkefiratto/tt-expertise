package com.ttexpertise.controller;

import com.ttexpertise.business.service.ExpertiseService;
import com.ttexpertise.model.dto.CreateExpertiseRequest;
import com.ttexpertise.model.dto.CreateExpertiseResponse;
import com.ttexpertise.model.dto.ReadExpertiseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expertises")
@Tag(name = "Expertise", description = "Araç ekspertiz yönetimi API'leri")
public class ExpertiseController {

    private final ExpertiseService expertiseService;

    public ExpertiseController(ExpertiseService expertiseService) {
        this.expertiseService = expertiseService;
    }

    @Operation(summary = "Son ekspertizi getir", description = "carId parametresine göre en son ekspertizi döner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Başarılı"),
        @ApiResponse(responseCode = "404", description = "Bulunamadı")
    })
    @GetMapping("/{carId}")
    public ReadExpertiseResponse read(@PathVariable String carId) {
        return expertiseService.readForCar(carId);
    }

    @Operation(summary = "Yeni ekspertiz oluştur", description = "Cevaplar ve fotoğraflarla birlikte yeni bir ekspertiz kaydeder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Validation hatası")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateExpertiseResponse create(@Valid @RequestBody CreateExpertiseRequest req) {
        UUID id = expertiseService.create(req);
        return new CreateExpertiseResponse(id);
    }
}