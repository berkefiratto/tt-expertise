package com.ttexpertise.business.impl;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertiseServiceImplTest {

    @Mock
    private ExpertiseRepository expertiseRepository;
    
    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private AnswerRepository answerRepository;
    
    @Mock
    private PhotoRepository photoRepository;
    
    @InjectMocks
    private ExpertiseServiceImpl expertiseService;

    @Test
    void shouldCreateExpertise() {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
            "car123",
            List.of(new CreateExpertiseRequest.AnswerPayload(
                1L, true, "Problem var", List.of("photo1.jpg", "photo2.jpg")
            ))
        );
        
        Question question = new Question();
        question.setId(1L);
        question.setText("Test question");
        
        Expertise savedExpertise = new Expertise();
        savedExpertise.setId(UUID.randomUUID());
        
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(expertiseRepository.save(any(Expertise.class))).thenReturn(savedExpertise);
        when(answerRepository.save(any(Answer.class))).thenReturn(new Answer());
        when(photoRepository.save(any(Photo.class))).thenReturn(new Photo());
        
        // When
        UUID result = expertiseService.create(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(expertiseRepository).save(any(Expertise.class));
        verify(questionRepository).findById(1L);
        verify(answerRepository).save(any(Answer.class));
        verify(photoRepository, times(2)).save(any(Photo.class));
    }

    @Test
    void shouldThrowExceptionWhenNoPhotoForYesAnswer() {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
            "car123",
            List.of(new CreateExpertiseRequest.AnswerPayload(
                1L, true, "Problem var", List.of() // Boş fotoğraf listesi
            ))
        );
        
        // When & Then
        assertThatThrownBy(() -> expertiseService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Evet seçilince en az 1 fotoğraf gerekli");
    }

    @Test
    void shouldThrowExceptionWhenTooManyPhotos() {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
            "car123",
            List.of(new CreateExpertiseRequest.AnswerPayload(
                1L, true, "Problem var", List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg") // 4 fotoğraf
            ))
        );
        
        // When & Then
        assertThatThrownBy(() -> expertiseService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("En fazla 3 fotoğraf yüklenebilir");
    }

    @Test
    void shouldCreateExpertiseWithNoAnswer() {
        // Given
        CreateExpertiseRequest request = new CreateExpertiseRequest(
            "car123",
            List.of(new CreateExpertiseRequest.AnswerPayload(
                1L, false, null, List.of() // "Hayır, yok" seçimi
            ))
        );
        
        Question question = new Question();
        question.setId(1L);
        question.setText("Test question");
        
        Expertise savedExpertise = new Expertise();
        savedExpertise.setId(UUID.randomUUID());
        
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(expertiseRepository.save(any(Expertise.class))).thenReturn(savedExpertise);
        when(answerRepository.save(any(Answer.class))).thenReturn(new Answer());
        
        // When
        UUID result = expertiseService.create(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(expertiseRepository).save(any(Expertise.class));
        verify(questionRepository).findById(1L);
        verify(answerRepository).save(any(Answer.class));
        verify(photoRepository, never()).save(any(Photo.class)); // Fotoğraf kaydedilmemeli
    }

    @Test
    void shouldReadExpertiseForCar() {
        // Given
        String carId = "car123";
        
        Question question1 = new Question();
        question1.setId(1L);
        question1.setText("Multimedyada problem var mı?");
        question1.setActive(true);
        
        Question question2 = new Question();
        question2.setId(2L);
        question2.setText("Ruhsatta eksiklik var mı?");
        question2.setActive(true);
        
        when(questionRepository.findAllByActiveTrueOrderByIdAsc())
            .thenReturn(List.of(question1, question2));
        when(expertiseRepository.findTopByCarIdOrderByCreatedAtDesc(carId))
            .thenReturn(Optional.empty());
        
        // When
        ReadExpertiseResponse result = expertiseService.readForCar(carId);
        
        // Then
        assertThat(result.carId()).isEqualTo(carId);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).questionId()).isEqualTo(1L);
        assertThat(result.items().get(0).text()).isEqualTo("Multimedyada problem var mı?");
        assertThat(result.items().get(0).previous().answeredYes()).isFalse();
    }

    @Test
    void shouldReadExpertiseWithPreviousAnswers() {
        // Given
        String carId = "car123";
        
        Question question1 = new Question();
        question1.setId(1L);
        question1.setText("Multimedyada problem var mı?");
        question1.setActive(true);
        
        Question question2 = new Question();
        question2.setId(2L);
        question2.setText("Ruhsatta eksiklik var mı?");
        question2.setActive(true);
        
        // Önceki ekspertiz
        Expertise previousExpertise = new Expertise();
        previousExpertise.setId(UUID.randomUUID());
        previousExpertise.setCarId(carId);
        
        Answer previousAnswer = new Answer();
        previousAnswer.setId(UUID.randomUUID());
        previousAnswer.setValue(true);
        previousAnswer.setDescription("Evet, problem var");
        previousAnswer.setQuestion(question1);
        
        Photo photo1 = new Photo();
        photo1.setId(UUID.randomUUID());
        photo1.setUrl("photo1.jpg");
        photo1.setAnswer(previousAnswer);
        
        Photo photo2 = new Photo();
        photo2.setId(UUID.randomUUID());
        photo2.setUrl("photo2.jpg");
        photo2.setAnswer(previousAnswer);
        
        previousAnswer.setPhotos(List.of(photo1, photo2));
        previousExpertise.setAnswers(List.of(previousAnswer));
        
        when(questionRepository.findAllByActiveTrueOrderByIdAsc())
            .thenReturn(List.of(question1, question2));
        when(expertiseRepository.findTopByCarIdOrderByCreatedAtDesc(carId))
            .thenReturn(Optional.of(previousExpertise));
        
        // When
        ReadExpertiseResponse result = expertiseService.readForCar(carId);
        
        // Then
        assertThat(result.carId()).isEqualTo(carId);
        assertThat(result.items()).hasSize(2);
        
        // İlk soru için önceki cevap var
        ReadExpertiseResponse.QuestionItem firstQuestion = result.items().get(0);
        assertThat(firstQuestion.questionId()).isEqualTo(1L);
        assertThat(firstQuestion.previous().answeredYes()).isTrue();
        assertThat(firstQuestion.previous().description()).isEqualTo("Evet, problem var");
        assertThat(firstQuestion.previous().photoUrls()).containsExactly("photo1.jpg", "photo2.jpg");
        
        // İkinci soru için önceki cevap yok
        ReadExpertiseResponse.QuestionItem secondQuestion = result.items().get(1);
        assertThat(secondQuestion.questionId()).isEqualTo(2L);
        assertThat(secondQuestion.previous().answeredYes()).isFalse();
        assertThat(secondQuestion.previous().photoUrls()).isEmpty();
    }
}