package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import com.velox.framework.totp.common.error.TotpErrorCode;
import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.exception.TotpCodecException;
import com.velox.framework.totp.exception.TotpComputeException;
import com.velox.framework.totp.properties.VeloxTotpProperties;
import com.velox.framework.totp.support.codec.Base32Codec;
import com.velox.framework.totp.support.type.TotpAlgorithm;
import com.velox.framework.totp.support.uri.OtpAuthUris;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.regex.Pattern;

/**
 * 标准 RFC 6238 实现，使用 RFC 4226 的动态截断生成口令。
 * 客户端兼容矩阵参见 {@link TotpService}。
 */
public class DefaultTotpService implements TotpService {

    private static final Pattern NUMERIC = Pattern.compile("^\\d+$");
    private static final int HOTP_COUNTER_BYTES = 8;
    private static final int DECIMAL_BASE = 10;
    private static final char ZERO_PAD = '0';

    private final VeloxTotpProperties properties;
    private final SecureRandom random;
    private final Clock clock;

    public DefaultTotpService(VeloxTotpProperties properties) {
        this(properties, new SecureRandom(), Clock.systemUTC());
    }

    public DefaultTotpService(VeloxTotpProperties properties, SecureRandom random, Clock clock) {
        this.properties = properties;
        this.random = random;
        this.clock = clock;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public TotpSecret generateSecret() {
        byte[] bytes = new byte[properties.getSecretSizeBytes()];
        random.nextBytes(bytes);
        return new TotpSecret(
                Base32Codec.encode(bytes),
                properties.getAlgorithm(),
                properties.getDigits(),
                properties.getPeriodSeconds()
        );
    }

    @Override
    public String buildOtpAuthUri(TotpSecret secret, String accountName) {
        return OtpAuthUris.buildTotp(
                properties.getIssuer(),
                accountName,
                secret.base32(),
                secret.algorithm(),
                secret.digits(),
                secret.periodSeconds()
        );
    }

    @Override
    public TotpProvisioning provision(String accountName) {
        TotpSecret secret = generateSecret();
        String uri = buildOtpAuthUri(secret, accountName);
        return new TotpProvisioning(secret, uri, properties.getIssuer(), accountName);
    }

    @Override
    public String currentCode(TotpSecret secret) {
        byte[] key = decodeSecret(secret.base32());
        long counter = clock.instant().getEpochSecond() / secret.periodSeconds();
        return formatCode(generateCode(key, counter, secret.algorithm(), secret.digits()), secret.digits());
    }

    @Override
    public TotpVerifyResult verify(TotpSecret secret, String code) {
        TotpVerifyResult formatCheck = validateCodeFormat(code, secret.digits());
        if (formatCheck != null) {
            return formatCheck;
        }
        if (secret.base32() == null || secret.base32().isBlank()) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_BLANK,
                    TotpCommonMessages.SECRET_MUST_NOT_BE_BLANK);
        }
        if (!Base32Codec.isValid(secret.base32())) {
            return TotpVerifyResult.failure(TotpErrorCode.SECRET_INVALID,
                    TotpCommonMessages.SECRET_DECODE_FAILED);
        }
        byte[] key = decodeSecret(secret.base32());
        long counter = clock.instant().getEpochSecond() / secret.periodSeconds();
        int window = properties.getVerifyWindowSteps();
        for (int offset = -window; offset <= window; offset++) {
            int candidate = generateCode(key, counter + offset, secret.algorithm(), secret.digits());
            if (constantTimeEquals(formatCode(candidate, secret.digits()), code.trim())) {
                return TotpVerifyResult.success(offset);
            }
        }
        return TotpVerifyResult.failure(TotpErrorCode.CODE_MISMATCH,
                TotpCommonMessages.CODE_MISMATCH);
    }

    @Override
    public TotpVerifyResult verify(String base32Secret, String code) {
        return verify(new TotpSecret(
                base32Secret,
                properties.getAlgorithm(),
                properties.getDigits(),
                properties.getPeriodSeconds()
        ), code);
    }

    private TotpVerifyResult validateCodeFormat(String code, int digits) {
        if (code == null || code.isBlank()) {
            return TotpVerifyResult.failure(TotpErrorCode.CODE_BLANK,
                    TotpCommonMessages.CODE_MUST_NOT_BE_BLANK);
        }
        String trimmed = code.trim();
        if (trimmed.length() != digits || !NUMERIC.matcher(trimmed).matches()) {
            return TotpVerifyResult.failure(TotpErrorCode.CODE_FORMAT_INVALID,
                    TotpCommonMessages.CODE_FORMAT_INVALID.formatted(digits));
        }
        return null;
    }

    private byte[] decodeSecret(String base32Secret) {
        try {
            return Base32Codec.decode(base32Secret);
        } catch (TotpCodecException ex) {
            throw new TotpComputeException(TotpCommonMessages.SECRET_DECODE_FAILED, ex);
        }
    }

    private int generateCode(byte[] key, long counter, TotpAlgorithm algorithm, int digits) {
        byte[] data = new byte[HOTP_COUNTER_BYTES];
        for (int i = HOTP_COUNTER_BYTES - 1; i >= 0; i--) {
            data[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }
        byte[] hash = hmac(key, data, algorithm);
        int offset = hash[hash.length - 1] & 0x0F;
        int binary =
                ((hash[offset] & 0x7F) << 24)
                        | ((hash[offset + 1] & 0xFF) << 16)
                        | ((hash[offset + 2] & 0xFF) << 8)
                        | (hash[offset + 3] & 0xFF);
        int modulus = (int) Math.pow(DECIMAL_BASE, digits);
        return binary % modulus;
    }

    private byte[] hmac(byte[] key, byte[] data, TotpAlgorithm algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm.hmacName());
            mac.init(new SecretKeySpec(key, algorithm.hmacName()));
            return mac.doFinal(data);
        } catch (GeneralSecurityException ex) {
            throw new TotpComputeException(TotpCommonMessages.HMAC_COMPUTE_FAILED, ex);
        }
    }

    private String formatCode(int code, int digits) {
        StringBuilder sb = new StringBuilder(Integer.toString(code));
        while (sb.length() < digits) {
            sb.insert(0, ZERO_PAD);
        }
        return sb.toString();
    }

    /**
     * 常数时间比较，避免侧信道泄露。
     */
    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ actual.charAt(i);
        }
        return diff == 0;
    }
}
