package com.english.learning.dto;

public class SpeakingResultDTO {
    private String referenceText;
    private String transcribedText;
    private int score; // 0-100
    private String feedback;
    private String audioUrl; // Cloudinary URL of current audio

    // Nested: Kết quả best (nếu có)
    private BestResult bestResult;

    public SpeakingResultDTO() {
    }

    // --- Getters & Setters ---

    public String getReferenceText() {
        return referenceText;
    }

    public void setReferenceText(String referenceText) {
        this.referenceText = referenceText;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public BestResult getBestResult() {
        return bestResult;
    }

    public void setBestResult(BestResult bestResult) {
        this.bestResult = bestResult;
    }

    // --- Nested DTO cho Best Result ---
    public static class BestResult {
        private int score;
        private String transcribedText;
        private String feedback;
        private String audioUrl;

        public BestResult() {
        }

        public BestResult(int score, String transcribedText, String feedback, String audioUrl) {
            this.score = score;
            this.transcribedText = transcribedText;
            this.feedback = feedback;
            this.audioUrl = audioUrl;
        }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getTranscribedText() { return transcribedText; }
        public void setTranscribedText(String transcribedText) { this.transcribedText = transcribedText; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public String getAudioUrl() { return audioUrl; }
        public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    }
}
