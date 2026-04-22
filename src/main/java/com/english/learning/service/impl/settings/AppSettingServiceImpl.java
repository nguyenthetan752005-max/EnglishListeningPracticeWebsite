package com.english.learning.service.impl.settings;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.AppSetting;
import com.english.learning.repository.AppSettingRepository;
import com.english.learning.service.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

    private final AppSettingRepository appSettingRepository;

    @Override
    @Cacheable(value = "appSettings", key = "'singleton'")
    public AppSetting getSettings() {
        return getOrCreateSettings();
    }

    @Override
    public AppSettingForm getSettingForm() {
        AppSetting settings = getSettings();
        AppSettingForm form = new AppSettingForm();
        form.setSiteName(settings.getSiteName());
        form.setSeoMetaDescription(settings.getSeoMetaDescription());
        form.setMaxRecentUsersOnDashboard(settings.getMaxRecentUsersOnDashboard());
        form.setSpeakingPassThreshold(settings.getSpeakingPassThreshold());
        form.setAllowUserRegistration(Boolean.TRUE.equals(settings.getAllowUserRegistration()));
        form.setOnlineTimeoutMinutes(settings.getOnlineTimeoutMinutes());
        return form;
    }

    @Override
    @CacheEvict(value = "appSettings", allEntries = true)
    public void updateSettings(AppSettingForm form) {
        AppSetting settings = getSettings();
        settings.setSiteName(cleanSiteName(form.getSiteName()));
        settings.setSeoMetaDescription(cleanSeoMetaDescription(form.getSeoMetaDescription()));
        settings.setMaxRecentUsersOnDashboard(form.getMaxRecentUsersOnDashboard());
        settings.setSpeakingPassThreshold(form.getSpeakingPassThreshold());
        settings.setAllowUserRegistration(form.isAllowUserRegistration());
        if (form.getOnlineTimeoutMinutes() != null) {
            settings.setOnlineTimeoutMinutes(form.getOnlineTimeoutMinutes());
        }
        appSettingRepository.save(settings);
    }

    @Override
    public String getSiteName() {
        return getSettings().getSiteName();
    }

    @Override
    public String getSeoMetaDescription() {
        return getSettings().getSeoMetaDescription();
    }

    @Override
    public int getMaxRecentUsersOnDashboard() {
        return getSettings().getMaxRecentUsersOnDashboard();
    }

    @Override
    public int getSpeakingPassThreshold() {
        return getSettings().getSpeakingPassThreshold();
    }

    @Override
    public boolean isUserRegistrationAllowed() {
        return Boolean.TRUE.equals(getSettings().getAllowUserRegistration());
    }

    @Override
    public int getOnlineTimeoutMinutes() {
        Integer timeout = getSettings().getOnlineTimeoutMinutes();
        return timeout != null ? timeout : AppSetting.DEFAULT_ONLINE_TIMEOUT_MINUTES;
    }

    private AppSetting getOrCreateSettings() {
        return appSettingRepository.findById(AppSetting.SINGLETON_ID)
                .orElseGet(() -> appSettingRepository.save(new AppSetting()));
    }

    private String cleanSiteName(String siteName) {
        return StringUtils.hasText(siteName) ? siteName.trim() : AppSetting.DEFAULT_SITE_NAME;
    }

    private String cleanSeoMetaDescription(String seoMetaDescription) {
        return StringUtils.hasText(seoMetaDescription)
                ? seoMetaDescription.trim()
                : AppSetting.DEFAULT_SEO_META_DESCRIPTION;
    }
}
