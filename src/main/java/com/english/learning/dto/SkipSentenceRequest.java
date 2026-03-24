package com.english.learning.dto;

import jakarta.validation.constraints.NotNull;

public class SkipSentenceRequest {

    @NotNull(message = "sentenceId is required")
    private Long sentenceId;

    public Long getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(Long sentenceId) {
        this.sentenceId = sentenceId;
    }
}
