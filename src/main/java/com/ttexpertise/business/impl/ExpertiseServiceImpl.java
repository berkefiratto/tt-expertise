package com.ttexpertise.business.impl;

import com.ttexpertise.business.service.ExpertiseService;
import com.ttexpertise.model.dto.CreateExpertiseRequest;
import com.ttexpertise.model.dto.ReadExpertiseResponse;
import com.ttexpertise.model.entity.Answer;
import com.ttexpertise.model.entity.Expertise;
import com.ttexpertise.model.entity.Photo;
import com.ttexpertise.model.entity.Question;
import com.ttexpertise.repository.AnswerRepository;
import com.ttexpertise.repository.ExpertiseRepository;
import com.ttexpertise.repository.PhotoRepository;
import com.ttexpertise.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpertiseServiceImpl implements ExpertiseService {

    private final ExpertiseRepository expertiseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final PhotoRepository photoRepository;

    public ExpertiseServiceImpl(ExpertiseRepository expertiseRepository,
                                QuestionRepository questionRepository,
                                AnswerRepository answerRepository,
                                PhotoRepository photoRepository) {
        this.expertiseRepository = expertiseRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.photoRepository = photoRepository;
    }

    @Override
    public ReadExpertiseResponse readForCar(String carId) {
        // 1. Tüm aktif soruları getir
        List<Question> questions = questionRepository.findAllByActiveTrueOrderByIdAsc();

        // 2. En son ekspertizi bul
        Optional<Expertise> lastExpertise = expertiseRepository.findTopByCarIdOrderByCreatedAtDesc(carId);

        // 3. Response oluştur
        List<ReadExpertiseResponse.QuestionItem> questionItems = questions.stream()
                .map(question -> {
                    // Bu soru için önceki cevabı bul
                    ReadExpertiseResponse.Previous previous = lastExpertise
                            .flatMap(expertise -> expertise.getAnswers().stream()
                                    .filter(answer -> answer.getQuestion().getId().equals(question.getId()))
                                    .findFirst()
                                    .map(answer -> new ReadExpertiseResponse.Previous(
                                            answer.isValue(),
                                            answer.getDescription(),
                                            answer.getPhotos().stream()
                                                    .map(Photo::getUrl)
                                                    .toList()
                                    )))
                            .orElse(new ReadExpertiseResponse.Previous(false, null, List.of()));

                    return new ReadExpertiseResponse.QuestionItem(
                            question.getId(),
                            question.getText(),
                            previous
                    );
                })
                .toList();

        return new ReadExpertiseResponse(carId, questionItems);
    }

    @Override
    @Transactional
    public UUID create(CreateExpertiseRequest request) {
        // 1. Validation: "Evet, var" seçilince fotoğraf zorunlu
        validateAnswers(request.answers());

        // 2. Expertise oluştur
        Expertise expertise = new Expertise();
        expertise.setCarId(request.carId());
        expertise = expertiseRepository.save(expertise);

        // 3. Her answer için Answer + Photo'ları kaydet
        for (CreateExpertiseRequest.AnswerPayload answerPayload : request.answers()) {
            // Answer oluştur
            Answer answer = new Answer();
            answer.setExpertise(expertise);
            answer.setQuestion(questionRepository.findById(answerPayload.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + answerPayload.questionId())));
            answer.setValue(answerPayload.value());
            answer.setDescription(answerPayload.description());
            answer = answerRepository.save(answer);

            // Photo'ları kaydet
            for (String photoUrl : answerPayload.photoUrls()) {
                Photo photo = new Photo();
                photo.setAnswer(answer);
                photo.setUrl(photoUrl);
                photoRepository.save(photo);
            }
        }

        return expertise.getId();
    }

    private void validateAnswers(List<CreateExpertiseRequest.AnswerPayload> answers) {
        for (CreateExpertiseRequest.AnswerPayload answer : answers) {
            if (answer.value() && (answer.photoUrls() == null || answer.photoUrls().isEmpty())) {
                throw new IllegalArgumentException("Evet seçilince en az 1 fotoğraf gerekli");
            }
            if (answer.value() && answer.photoUrls().size() > 3) {
                throw new IllegalArgumentException("En fazla 3 fotoğraf yüklenebilir");
            }
        }
    }
}
