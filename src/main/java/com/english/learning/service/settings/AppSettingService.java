package com.english.learning.service.settings;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.AppSetting;

public interface AppSettingService {
    AppSetting getSettings();
    AppSettingForm getSettingForm();
    void updateSettings(AppSettingForm form);
    String getSiteName();
    String getSeoMetaDescription();
    int getMaxRecentUsersOnDashboard();
    int getSpeakingPassThreshold();
    boolean isUserRegistrationAllowed();
}
