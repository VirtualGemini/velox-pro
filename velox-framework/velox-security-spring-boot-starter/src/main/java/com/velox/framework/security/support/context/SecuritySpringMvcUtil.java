package com.velox.framework.security.support.context;

import cn.dev33.satoken.exception.NotWebContextException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class SecuritySpringMvcUtil {

    private SecuritySpringMvcUtil() {
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw (NotWebContextException) new NotWebContextException("Current context is not a web request").setCode(20101);
        }
        return attributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw (NotWebContextException) new NotWebContextException("Current context is not a web request").setCode(20101);
        }
        return attributes.getResponse();
    }

    public static boolean isWeb() {
        return RequestContextHolder.getRequestAttributes() != null;
    }
}
