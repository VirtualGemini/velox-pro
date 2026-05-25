package com.velox.totp.support.uri;

import com.velox.totp.common.message.TotpCommonMessages;
import com.velox.totp.exception.TotpConfigException;
import com.velox.totp.support.type.TotpAlgorithm;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 构造 otpauth:// URI，遵循 Google Authenticator key URI 规范：
 * otpauth://totp/{Issuer}:{accountName}?secret={base32}&issuer={Issuer}&algorithm={ALG}&digits={N}&period={S}
 * <p>所有受信任的客户端（Google / Microsoft / Authy / 1Password / Bitwarden / 腾讯 / 国产 / Aegis）均按此规范解析。</p>
 */
public final class OtpAuthUris {

    private static final String SCHEME = "otpauth://totp/";

    private OtpAuthUris() {
    }

    public static String buildTotp(String issuer,
                                   String accountName,
                                   String base32Secret,
                                   TotpAlgorithm algorithm,
                                   int digits,
                                   int periodSeconds) {
        if (issuer == null || issuer.isBlank()) {
            throw new TotpConfigException(TotpCommonMessages.ISSUER_MUST_NOT_BE_BLANK);
        }
        if (accountName == null || accountName.isBlank()) {
            throw new TotpConfigException(TotpCommonMessages.ACCOUNT_MUST_NOT_BE_BLANK);
        }
        String encodedIssuer = encode(issuer);
        String encodedAccount = encode(accountName);
        StringBuilder sb = new StringBuilder(128);
        sb.append(SCHEME)
                .append(encodedIssuer).append(':').append(encodedAccount)
                .append("?secret=").append(base32Secret)
                .append("&issuer=").append(encodedIssuer)
                .append("&algorithm=").append(algorithm.otpAuthName())
                .append("&digits=").append(digits)
                .append("&period=").append(periodSeconds);
        return sb.toString();
    }

    /**
     * URL 编码 issuer / account，再把 + 替换为 %20，避免被部分客户端误解为空格之外的语义。
     */
    private static String encode(String value) {
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }
}
