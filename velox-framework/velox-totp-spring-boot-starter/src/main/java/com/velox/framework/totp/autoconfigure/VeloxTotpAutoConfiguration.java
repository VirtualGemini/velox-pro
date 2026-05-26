package com.velox.framework.totp.autoconfigure;

import com.velox.framework.totp.api.service.TotpService;
import com.velox.framework.totp.common.prefix.TotpPropertyPrefixes;
import com.velox.framework.totp.core.DefaultTotpService;
import com.velox.framework.totp.noop.DisabledTotpService;
import com.velox.framework.totp.properties.VeloxTotpProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(VeloxTotpProperties.class)
public class VeloxTotpAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = TotpPropertyPrefixes.ENABLED, havingValue = TotpPropertyPrefixes.TRUE)
    @ConditionalOnMissingBean
    public TotpService totpService(VeloxTotpProperties properties) {
        properties.validate();
        return new DefaultTotpService(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = TotpPropertyPrefixes.ENABLED, havingValue = TotpPropertyPrefixes.FALSE, matchIfMissing = true)
    @ConditionalOnMissingBean
    public TotpService disabledTotpService() {
        return new DisabledTotpService();
    }
}
