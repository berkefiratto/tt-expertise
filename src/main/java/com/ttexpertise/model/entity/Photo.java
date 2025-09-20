package com.ttexpertise.model.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "photo")
public class Photo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_photo_answer"))
    private Answer answer;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @PrePersist
    void pre() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}