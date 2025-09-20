package com.ttexpertise.model.dto;

import java.util.List;

public record ReadExpertiseResponse(
        String carId,
        List<QuestionItem> items
) {
    public record QuestionItem(
            Long questionId, String text, Previous previous
    ) {}
    public record Previous(
            boolean answeredYes, String description, List<String> photoUrls
    ) {}
}