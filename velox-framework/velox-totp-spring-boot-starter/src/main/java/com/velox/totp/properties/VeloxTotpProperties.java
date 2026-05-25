package com.velox.totp.properties;

import com.velox.totp.common.message.TotpCommonMessages;
import com.velox.totp.common.prefix.TotpPropertyPrefixes;
import com.velox.totp.exception.TotpConfigException;
import com.velox.totp.support.type.TotpAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = TotpPropertyPrefixes.TOTP)
public class VeloxTotpProperties {

    /**
     * 默认关闭。仅当业务模块明确启用 MFA-TOTP 时再打开。
     */
    private boolean enabled = false;

    /**
     * 二维码 / otpauth URI 中显示的发行方名称，建议为产品名或租户名。
     */
    private String issuer = "Velox";

    /**
     * 口令位数，所有目标客户端均兼容 6/7/8 位；常见值为 6。
     */
    private int digits = 6;

    /**
     * 时间步长，秒。RFC 6238 推荐 30 秒，所有目标客户端默认都按 30 秒解析。
     */
    private int periodSeconds = 30;

    /**
     * HMAC 算法。默认 SHA1 以兼容 Google / Microsoft / 腾讯等仅支持 SHA1 的客户端。
     */
    private TotpAlgorithm algorithm = TotpAlgorithm.SHA1;

    /**
     * 生成新 secret 时的字节长度。RFC 4226 推荐 SHA1 ≥ 160 bit（20 字节），此处给 20 字节。
     */
    private int secretSizeBytes = 20;

    /**
     * 校验时允许的相邻时间步数：1 表示当前窗口 ± 1 步（±30 秒），有效补偿设备时钟漂移。
     */
    private int verifyWindowSteps = 1;

    public void validate() {
        if (digits < 6 || digits > 10) {
            throw new TotpConfigException(TotpCommonMessages.DIGITS_OUT_OF_RANGE);
        }
        if (periodSeconds < 1) {
            throw new TotpConfigException(TotpCommonMessages.PERIOD_OUT_OF_RANGE);
        }
        if (secretSizeBytes < 16) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_SIZE_OUT_OF_RANGE);
        }
        if (verifyWindowSteps < 0) {
            throw new TotpConfigException(TotpCommonMessages.VERIFY_WINDOW_OUT_OF_RANGE);
        }
        if (algorithm == null) {
            throw new TotpConfigException(TotpCommonMessages.ALGORITHM_MUST_NOT_BE_NULL);
        }
        if (enabled && (issuer == null || issuer.isBlank())) {
            throw new TotpConfigException(TotpCommonMessages.ISSUER_MUST_NOT_BE_BLANK);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public TotpAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(TotpAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getSecretSizeBytes() {
        return secretSizeBytes;
    }

    public void setSecretSizeBytes(int secretSizeBytes) {
        this.secretSizeBytes = secretSizeBytes;
    }

    public int getVerifyWindowSteps() {
        return verifyWindowSteps;
    }

    public void setVerifyWindowSteps(int verifyWindowSteps) {
        this.verifyWindowSteps = verifyWindowSteps;
    }
}
