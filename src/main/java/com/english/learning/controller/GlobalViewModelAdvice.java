package com.english.learning.controller;

import com.english.learning.entity.AppSetting;
import com.english.learning.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalViewModelAdvice {

    private final AppSettingService appSettingService;

    @ModelAttribute("appSiteName")
    public String appSiteName() {
        return appSettingService.getSettings().getSiteName();
    }

    @ModelAttribute("appSeoMetaDescription")
    public String appSeoMetaDescription() {
        return appSettingService.getSettings().getSeoMetaDescription();
    }

    @ModelAttribute("registrationEnabled")
    public boolean registrationEnabled() {
        return appSettingService.isUserRegistrationAllowed();
    }

    @ModelAttribute("appSpeakingPassThreshold")
    public int appSpeakingPassThreshold() {
        return appSettingService.getSpeakingPassThreshold();
    }
}
