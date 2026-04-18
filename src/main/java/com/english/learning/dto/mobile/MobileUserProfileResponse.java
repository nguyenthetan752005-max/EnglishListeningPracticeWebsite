package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileUserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
    private Integer totalActiveTime;
    private String formattedActiveTime;
    private Integer activeTime7d;
    private Integer activeTime30d;
}
