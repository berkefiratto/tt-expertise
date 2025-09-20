package com.ttexpertise.integration;

import com.ttexpertise.TtExpertiseApplication;
import com.ttexpertise.model.entity.Expertise;
import com.ttexpertise.repository.ExpertiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TtExpertiseApplication.class)
@Testcontainers
@ActiveProfiles("test")
class OptimisticLockingTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ExpertiseRepository expertiseRepository;

    private Expertise expertise;

    @BeforeEach
    void setUp() {
        expertiseRepository.deleteAll();
        
        expertise = new Expertise();
        expertise.setCarId("CAR123");
        expertise = expertiseRepository.save(expertise);
    }

    @Test
    @Transactional
    void shouldDetectOptimisticLockingFailure() throws InterruptedException {
        // Given
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When - Two threads try to update the same entity simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // Reload entity to get current version
                    Expertise entity = expertiseRepository.findById(expertise.getId()).orElseThrow();
                    
                    // Simulate some processing time
                    Thread.sleep(100);
                    
                    // Update the entity
                    entity.setCarId("UPDATED_CAR_" + Thread.currentThread().getId());
                    expertiseRepository.save(entity);
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    if (e.getCause() instanceof org.springframework.orm.ObjectOptimisticLockingFailureException) {
                        failureCount.incrementAndGet();
                    } else {
                        e.printStackTrace();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        latch.await();
        executor.shutdown();

        // One should succeed, one should fail due to optimistic locking
        assertEquals(1, successCount.get(), "One update should succeed");
        assertEquals(1, failureCount.get(), "One update should fail due to optimistic locking");
    }

    @Test
    void shouldIncrementVersionOnUpdate() {
        // Given
        Long initialVersion = expertise.getVersion();
        assertNotNull(initialVersion, "Version should be set initially");

        // When
        expertise.setCarId("UPDATED_CAR");
        Expertise updated = expertiseRepository.save(expertise);

        // Then
        assertTrue(updated.getVersion() > initialVersion, 
                "Version should be incremented after update");
    }

    @Test
    void shouldSetVersionOnCreate() {
        // Given
        Expertise newExpertise = new Expertise();
        newExpertise.setCarId("NEW_CAR");

        // When
        Expertise saved = expertiseRepository.save(newExpertise);

        // Then
        assertNotNull(saved.getVersion(), "Version should be set on create");
        assertEquals(0L, saved.getVersion(), "Initial version should be 0");
    }
}