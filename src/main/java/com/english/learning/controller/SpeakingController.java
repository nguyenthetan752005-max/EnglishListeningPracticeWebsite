package com.english.learning.controller;

import com.english.learning.model.SpeakingResult;
import com.english.learning.service.ISpeakingResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpeakingController {

    @Autowired
    private ISpeakingResultService speakingResultService;

    @PostMapping("/speaking/submit")
    @ResponseBody
    public SpeakingResult submitSpeaking(
            @RequestParam Long userId,
            @RequestParam Long sentenceId,
            @RequestParam Double accuracy,
            @RequestParam String recognizedText,
            @RequestParam String userAudioUrl) {
        return speakingResultService.saveSpeakingResult(userId, sentenceId, accuracy, recognizedText, userAudioUrl);
    }
}
