package com.velox.framework.security.support.authorization;

import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.framework.security.api.authorization.SecurityPermissionProvider;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.exception.SecurityAuthenticationException;
import com.velox.framework.security.exception.SecurityAuthorizationException;

public class DefaultSecurityAuthorizationService implements SecurityAuthorizationService {

    private final SecuritySessionService securitySessionService;
    private final SecurityPermissionProvider securityPermissionProvider;

    public DefaultSecurityAuthorizationService(SecuritySessionService securitySessionService,
                                               SecurityPermissionProvider securityPermissionProvider) {
        this.securitySessionService = securitySessionService;
        this.securityPermissionProvider = securityPermissionProvider;
    }

    @Override
    public void checkAuthenticated() {
        if (!securitySessionService.isAuthenticated()) {
            throw new SecurityAuthenticationException("Login required");
        }
    }

    @Override
    public void checkPermission(String permission) {
        checkAuthenticated();
        String loginId = securitySessionService.requireCurrentLoginId();
        if (permission == null || permission.isBlank()) {
            throw new SecurityAuthorizationException("Permission is required");
        }
        boolean allowed = securityPermissionProvider.getPermissions(loginId).stream()
                .anyMatch(mark -> permission.trim().equals(mark));
        if (!allowed) {
            throw new SecurityAuthorizationException("Permission denied");
        }
    }
}
