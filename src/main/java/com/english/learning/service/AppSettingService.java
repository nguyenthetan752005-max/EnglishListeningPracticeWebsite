package com.english.learning.service;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.AppSetting;

public interface AppSettingService {
    AppSetting getSettings();
    AppSettingForm getSettingForm();
    AppSetting updateSettings(AppSettingForm form);
    boolean isUserRegistrationAllowed();
    int getSpeakingPassThreshold();
}
