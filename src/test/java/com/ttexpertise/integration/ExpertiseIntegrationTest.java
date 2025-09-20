package com.ttexpertise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttexpertise.model.dto.CreateExpertiseRequest;
import com.ttexpertise.model.dto.ReadExpertiseResponse;
import com.ttexpertise.repository.AnswerRepository;
import com.ttexpertise.repository.ExpertiseRepository;
import com.ttexpertise.repository.PhotoRepository;
import com.ttexpertise.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ExpertiseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private PhotoRepository photoRepository;

    private MockMvc mockMvc;
    private com.ttexpertise.model.entity.Question q1;
    private com.ttexpertise.model.entity.Question q2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up data
        photoRepository.deleteAll();
        answerRepository.deleteAll();
        expertiseRepository.deleteAll();
        questionRepository.deleteAll();
        
        // Insert test questions
        q1 = new com.ttexpertise.model.entity.Question();
        q1.setText("Test question 1");
        q1.setActive(true);
        
        q2 = new com.ttexpertise.model.entity.Question();
        q2.setText("Test question 2");
        q2.setActive(true);
        
        questionRepository.saveAll(List.of(q1, q2));
    }

    @Test
    @Transactional
    void shouldCreateAndReadExpertise() throws Exception {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
                "CAR123",
                List.of(
                        new CreateExpertiseRequest.AnswerPayload(
                                q1.getId(), true, "Test description", 
                                List.of("http://example.com/photo1.jpg", "http://example.com/photo2.jpg")
                        ),
                        new CreateExpertiseRequest.AnswerPayload(
                                q2.getId(), false, null, List.of()
                        )
                )
        );

        // When - Create expertise
        String createResponse = mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Read expertise
        mockMvc.perform(get("/api/v1/expertises/CAR123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.carId").value("CAR123"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].questionId").value(q1.getId()))
                .andExpect(jsonPath("$.items[0].text").value("Test question 1"))
                .andExpect(jsonPath("$.items[0].previous.answeredYes").value(true))
                .andExpect(jsonPath("$.items[0].previous.description").value("Test description"))
                .andExpect(jsonPath("$.items[0].previous.photoUrls").isArray())
                .andExpect(jsonPath("$.items[0].previous.photoUrls.length()").value(2))
                .andExpect(jsonPath("$.items[1].questionId").value(q2.getId()))
                .andExpect(jsonPath("$.items[1].text").value("Test question 2"))
                .andExpect(jsonPath("$.items[1].previous.answeredYes").value(false));
    }

    @Test
    @Transactional
    void shouldReturnEmptyListForNonExistentCar() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/expertises/NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.carId").value("NONEXISTENT"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @Transactional
    void shouldValidatePhotoRequirements() throws Exception {
        // Given - Request with "yes" answer but no photos
        CreateExpertiseRequest invalidRequest = new CreateExpertiseRequest(
                "CAR123",
                List.of(
                        new CreateExpertiseRequest.AnswerPayload(
                                q1.getId(), true, "Test description", 
                                List.of() // No photos for "yes" answer
                        )
                )
        );

        // When & Then
        mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldValidatePhotoCount() throws Exception {
        // Given - Request with too many photos
        CreateExpertiseRequest invalidRequest = new CreateExpertiseRequest(
                "CAR123",
                List.of(
                        new CreateExpertiseRequest.AnswerPayload(
                                q1.getId(), true, "Test description", 
                                List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg") // Too many photos
                        )
                )
        );

        // When & Then
        mockMvc.perform(post("/api/v1/expertises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
