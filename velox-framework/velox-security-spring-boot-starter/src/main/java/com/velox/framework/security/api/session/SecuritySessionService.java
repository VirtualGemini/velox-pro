package com.velox.framework.security.api.session;

public interface SecuritySessionService {

    String login(String loginId);

    void logout();

    boolean isAuthenticated();

    String currentLoginIdOrNull();

    default String requireCurrentLoginId() {
        String loginId = currentLoginIdOrNull();
        if (loginId == null || loginId.isBlank()) {
            throw new com.velox.framework.security.exception.SecurityAuthenticationException("Login required");
        }
        return loginId;
    }

    String currentTokenOrNull();
}
