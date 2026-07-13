package com.sandbox.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeResponse {
    private Long userId;
    private List<FeaturePrivilege> privileges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturePrivilege {
        private String feature;
        private Boolean enabled;
    }
}
