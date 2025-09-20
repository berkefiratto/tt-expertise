package com.ttexpertise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttexpertise.business.service.ExpertiseService;
import com.ttexpertise.model.dto.CreateExpertiseRequest;
import com.ttexpertise.model.dto.CreateExpertiseResponse;
import com.ttexpertise.model.dto.ReadExpertiseResponse;
import com.ttexpertise.service.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpertiseController.class)
class ExpertiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpertiseService expertiseService;
    
    @MockBean
    private IdempotencyService idempotencyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReadExpertise() throws Exception {
        // Given
        String carId = "CAR123";
        ReadExpertiseResponse response = new ReadExpertiseResponse(
                carId,
                List.of(
                        new ReadExpertiseResponse.QuestionItem(
                                1L, "Test question", 
                                new ReadExpertiseResponse.Previous(false, null, List.of())
                        )
                )
        );
        
        when(expertiseService.readForCar(carId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/expertises/{carId}", carId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.carId").value(carId))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].questionId").value(1))
                .andExpect(jsonPath("$.items[0].text").value("Test question"));
    }

    @Test
    void shouldCreateExpertise() throws Exception {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
                "CAR123",
                List.of(
                        new CreateExpertiseRequest.AnswerPayload(
                                1L, true, "Test description", 
                                List.of("http://example.com/photo1.jpg")
                        )
                )
        );
        
        UUID expertiseId = UUID.randomUUID();
        when(expertiseService.create(any(CreateExpertiseRequest.class)))
                .thenReturn(expertiseId);
        when(idempotencyService.getResult(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expertiseId.toString()));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // Given
        CreateExpertiseRequest invalidRequest = new CreateExpertiseRequest(
                "", // Empty carId
                List.of()
        );

        // When & Then
        mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMissingRequiredFields() throws Exception {
        // Given
        String invalidJson = "{\"carId\":\"CAR123\"}"; // Missing answers

        // When & Then
        mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
