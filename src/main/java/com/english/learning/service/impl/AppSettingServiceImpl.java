package com.english.learning.service.impl;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.AppSetting;
import com.english.learning.repository.AppSettingRepository;
import com.english.learning.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

    private final AppSettingRepository appSettingRepository;

    @Override
    @Transactional
    public AppSetting getSettings() {
        return appSettingRepository.findById(AppSetting.SINGLETON_ID)
                .orElseGet(() -> appSettingRepository.save(new AppSetting()));
    }

    @Override
    @Transactional(readOnly = true)
    public AppSettingForm getSettingForm() {
        AppSetting settings = getSettings();
        AppSettingForm form = new AppSettingForm();
        form.setSiteName(settings.getSiteName());
        form.setSeoMetaDescription(settings.getSeoMetaDescription());
        form.setMaxRecentUsersOnDashboard(settings.getMaxRecentUsersOnDashboard());
        form.setSpeakingPassThreshold(settings.getSpeakingPassThreshold());
        form.setAllowUserRegistration(Boolean.TRUE.equals(settings.getAllowUserRegistration()));
        return form;
    }

    @Override
    @Transactional
    public AppSetting updateSettings(AppSettingForm form) {
        AppSetting settings = getSettings();
        settings.setSiteName(cleanSiteName(form.getSiteName()));
        settings.setSeoMetaDescription(cleanSeoMetaDescription(form.getSeoMetaDescription()));
        settings.setMaxRecentUsersOnDashboard(form.getMaxRecentUsersOnDashboard());
        settings.setSpeakingPassThreshold(form.getSpeakingPassThreshold());
        settings.setAllowUserRegistration(form.isAllowUserRegistration());
        return appSettingRepository.save(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserRegistrationAllowed() {
        return Boolean.TRUE.equals(getSettings().getAllowUserRegistration());
    }

    @Override
    @Transactional(readOnly = true)
    public int getSpeakingPassThreshold() {
        return getSettings().getSpeakingPassThreshold();
    }

    private String cleanSiteName(String siteName) {
        return StringUtils.hasText(siteName) ? siteName.trim() : "English Listening Practice";
    }

    private String cleanSeoMetaDescription(String seoMetaDescription) {
        return StringUtils.hasText(seoMetaDescription)
                ? seoMetaDescription.trim()
                : "Free English listening and speaking practice platform with real-world conversations.";
    }
}
