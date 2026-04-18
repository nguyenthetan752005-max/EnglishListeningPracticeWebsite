package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileLeaderboardEntryResponse {
    private Integer rank;
    private String username;
    private String initial;
    private String activeTime;
}
