package com.velox.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 兼容旧引用路径，实际配置定义已迁移到 security.properties 包。
 */
@ConfigurationProperties(prefix = "velox.security")
public class SecurityProperties extends com.velox.framework.security.properties.SecurityProperties {
}
