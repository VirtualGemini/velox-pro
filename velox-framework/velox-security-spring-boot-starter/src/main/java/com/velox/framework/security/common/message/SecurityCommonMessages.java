package com.velox.framework.security.common.message;

public final class SecurityCommonMessages {

    public static final String SECURITY_TOKEN_PROVIDER_NOT_FOUND =
            "No compatible SecurityTokenProvider found for configured mode/provider";

    public static final String SECURITY_TOKEN_PROVIDER_DUPLICATED =
            "Multiple SecurityTokenProvider implementations matched configured mode/provider";

    public static final String SECURITY_PERMISSION_PROVIDER_NOT_FOUND =
            "SecurityPermissionProvider is required and no compatible implementation was found";

    private SecurityCommonMessages() {
    }
}
