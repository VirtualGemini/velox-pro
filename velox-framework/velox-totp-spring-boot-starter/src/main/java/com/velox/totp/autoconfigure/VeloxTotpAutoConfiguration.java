package com.velox.totp.autoconfigure;

import com.velox.totp.api.service.TotpService;
import com.velox.totp.common.prefix.TotpPropertyPrefixes;
import com.velox.totp.core.DefaultTotpService;
import com.velox.totp.noop.DisabledTotpService;
import com.velox.totp.properties.VeloxTotpProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(VeloxTotpProperties.class)
public class VeloxTotpAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public TotpService totpService(VeloxTotpProperties properties) {
        properties.validate();
        return new DefaultTotpService(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = "enabled", havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean
    public TotpService disabledTotpService() {
        return new DisabledTotpService();
    }
}
