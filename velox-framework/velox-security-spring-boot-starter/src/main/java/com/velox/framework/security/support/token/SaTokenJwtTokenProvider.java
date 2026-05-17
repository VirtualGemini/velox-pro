package com.velox.framework.security.support.token;

import com.velox.framework.security.api.token.SecurityTokenEngine;
import com.velox.framework.security.api.token.SecurityTokenRuntime;
import com.velox.framework.security.core.token.AbstractSecurityTokenProvider;
import com.velox.framework.security.exception.SecurityConfigException;
import com.velox.framework.security.properties.SecurityProperties;
import org.springframework.util.StringUtils;

public class SaTokenJwtTokenProvider extends AbstractSecurityTokenProvider {

    public SaTokenJwtTokenProvider(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    public String provider() {
        return "satoken-jwt";
    }

    @Override
    public String mode() {
        return "jwt";
    }

    @Override
    protected void doCustomize(SecurityTokenRuntime runtime) {
        String strategy = normalize(securityProperties.getToken().getJwt().getStrategy());
        runtime.setJwtSecret(securityProperties.getToken().getJwt().getSecret());
        runtime.setWriteHeader(securityProperties.getToken().getJwt().isWriteHeader());
        runtime.setEngine(switch (strategy) {
            case "", "mixin" -> SecurityTokenEngine.JWT_MIXIN;
            case "simple" -> SecurityTokenEngine.JWT_SIMPLE;
            case "stateless" -> SecurityTokenEngine.JWT_STATELESS;
            default -> throw new SecurityConfigException("Unsupported jwt strategy: " + strategy);
        });
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }
}
