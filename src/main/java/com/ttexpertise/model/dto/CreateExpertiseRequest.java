package com.ttexpertise.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateExpertiseRequest(
        @NotBlank String carId,
        @NotEmpty List<AnswerPayload> answers
) {
    public record AnswerPayload(
            @NotNull Long questionId,
            @NotNull Boolean value,
            String description,
            List<@NotBlank String> photoUrls
    ) {
    }
}