package com.velox.module.system.auth.store;

public interface VerificationCodeStore {

    void saveCaptcha(String key, String code);

    VerificationResult consumeCaptcha(String key, String code);

    boolean captchaExists(String key);

    boolean trySaveResetCode(String email, String code);

    void invalidateResetCode(String email);

    VerificationResult verifyResetCode(String email, String code);

    boolean resetCodeExists(String email);

    /**
     * 保存登录验证码，包含发送频率限制；target 可为邮箱或手机号。
     */
    boolean trySaveLoginCode(String target, String code);

    void invalidateLoginCode(String target);

    VerificationResult verifyLoginCode(String target, String code);

    boolean loginCodeExists(String target);

    enum VerificationResult {
        MATCHED,
        INVALID,
        EXPIRED
    }
}
