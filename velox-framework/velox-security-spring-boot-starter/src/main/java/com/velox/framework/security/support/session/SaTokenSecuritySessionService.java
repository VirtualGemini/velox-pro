package com.velox.framework.security.support.session;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.exception.SecurityAuthenticationException;

public class SaTokenSecuritySessionService implements SecuritySessionService {

    @Override
    public String login(String loginId) {
        try {
            StpUtil.login(loginId);
            return StpUtil.getTokenValue();
        } catch (SaTokenException exception) {
            throw new SecurityAuthenticationException("Login failed", exception);
        }
    }

    @Override
    public void logout() {
        try {
            StpUtil.logout();
        } catch (SaTokenException exception) {
            throw new SecurityAuthenticationException("Logout failed", exception);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return StpUtil.isLogin();
    }

    @Override
    public String currentLoginIdOrNull() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : null;
    }

    @Override
    public String currentTokenOrNull() {
        return StpUtil.getTokenValue();
    }
}
