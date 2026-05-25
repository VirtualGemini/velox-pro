package com.velox.module.system.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserRole;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.CodeLoginCommand;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCodeSendCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.service.LoginService;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.domain.model.UserSession;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginServiceImpl implements LoginService {

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^1[3-9]\\d{9}$");
    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordCipherService passwordCipherService;
    private final SecurityProperties securityProperties;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final VerificationCodeStore verificationCodeStore;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;

    public LoginServiceImpl(UserMapper userMapper,
                            ProfileMapper profileMapper,
                            RoleMapper roleMapper,
                            UserRoleMapper userRoleMapper,
                            PasswordCipherService passwordCipherService,
                            SecurityProperties securityProperties,
                            SystemEntityIdGenerator entityIdGenerator,
                            ObjectProvider<EmailBuilder> emailBuilderProvider,
                            VerificationCodeStore verificationCodeStore,
                            ActiveUserStatusService activeUserStatusService,
                            SecuritySessionService securitySessionService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordCipherService = passwordCipherService;
        this.securityProperties = securityProperties;
        this.entityIdGenerator = entityIdGenerator;
        this.emailBuilderProvider = emailBuilderProvider;
        this.verificationCodeStore = verificationCodeStore;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public CaptchaDTO generateCaptcha() {
        CaptchaDTO dto = new CaptchaDTO();
        dto.setIsCaptchaOn(true);

        SpecCaptcha specCaptcha = new SpecCaptcha(120, 40, 4);
        String key = IdUtil.simpleUUID();
        verificationCodeStore.saveCaptcha(key, specCaptcha.text());

        dto.setCaptchaCodeKey(key);
        dto.setCaptchaCodeImg(specCaptcha.toBase64());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO login(LoginCommand command) {
        validateCaptchaIfPresent(command.getCaptchaCode(), command.getCaptchaCodeKey());

        String username = command.getUsername();
        String password = command.getPassword();

        if (username == null || username.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (password == null || password.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        User user = findUserByAccount(username);

        if (user == null) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        checkLoginLock(user);

        if (!passwordCipherService.matches(password, user.getPassword())) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (Integer.valueOf(4).equals(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        resetLoginFailCount(user);
        upgradePasswordIfNeeded(user, password);

        String sessionId = entityIdGenerator.nextId(UserSession.class);
        String token = securitySessionService.login(user.getId(), sessionId);
        try {
            activeUserStatusService.recordLogin(user.getId(), sessionId, token);
        } catch (RuntimeException exception) {
            try {
                securitySessionService.logout();
            } catch (RuntimeException ignored) {
                // 会话表写入失败时优先回滚当前 token，避免发出不可追踪的登录态。
            }
            throw exception;
        }

        return new TokenDTO(token, null);
    }

    @Override
    public void register(RegisterCommand command) {
        if (!command.getPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User existUser = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getUsername, command.getUsername())
        );

        if (existUser != null) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setId(entityIdGenerator.nextId(User.class));
        user.setUsername(command.getUsername());
        user.setPassword(passwordCipherService.encode(command.getPassword()));
        user.setStatus(1);
        user.setLoginFailCount(0);
        user.setDeleted(0);

        userMapper.insert(user);

        Profile profile = new Profile();
        profile.setId(entityIdGenerator.nextId(Profile.class));
        profile.setUserId(user.getId());
        profile.setNickname(command.getUsername());
        profile.setAvatar(buildDefaultAvatar(command.getUsername()));
        profile.setGender(0);
        profile.setDeleted(0);
        profileMapper.insert(profile);

        Role defaultRole = roleMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, "R_USER")
                .last("limit 1"));
        if (defaultRole != null && defaultRole.getId() != null) {
            UserRole userRole = new UserRole();
            userRole.setId(entityIdGenerator.nextId(UserRole.class));
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setDeleted(0);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public void sendResetPasswordCode(ForgotPasswordCodeCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveResetCode(email, code)) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("密码重置验证码")
                    .text(buildResetPasswordMailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateResetCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateResetCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyResetCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_ERROR);
        }

        if (passwordCipherService.matches(command.getNewPassword(), user.getPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordCipherService.encode(command.getNewPassword().trim()));
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout() {
        String userId = securitySessionService.currentLoginIdOrNull();
        String tokenValue = securitySessionService.currentTokenOrNull();
        securitySessionService.logout();
        activeUserStatusService.recordLogout(userId, tokenValue);
    }

    @Override
    public void sendLoginCode(LoginCodeSendCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if ("phone".equals(type)) {
            // 手机号验证码登录：占位，未来接入短信能力时再实现。
            throw new ApiException(BusinessErrorCode.PHONE_LOGIN_NOT_SUPPORTED);
        }
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveLoginCode(email, code)) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("登录验证码")
                    .text(buildLoginCodeMailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateLoginCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateLoginCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO loginByCode(CodeLoginCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if ("phone".equals(type)) {
            throw new ApiException(BusinessErrorCode.PHONE_LOGIN_NOT_SUPPORTED);
        }
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        checkLoginLock(user);

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyLoginCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_ERROR);
        }

        if (Integer.valueOf(4).equals(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        resetLoginFailCount(user);

        String sessionId = entityIdGenerator.nextId(UserSession.class);
        String token = securitySessionService.login(user.getId(), sessionId);
        try {
            activeUserStatusService.recordLogin(user.getId(), sessionId, token);
        } catch (RuntimeException exception) {
            try {
                securitySessionService.logout();
            } catch (RuntimeException ignored) {
                // 会话表写入失败时优先回滚当前 token，避免发出不可追踪的登录态。
            }
            throw exception;
        }

        return new TokenDTO(token, null);
    }

    private void validateCaptchaIfPresent(String captchaCode, String key) {
        boolean captchaCodeBlank = captchaCode == null || captchaCode.isBlank();
        boolean keyBlank = key == null || key.isBlank();

        if (captchaCodeBlank && keyBlank) {
            return;
        }

        if (captchaCodeBlank || keyBlank) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }

        VerificationCodeStore.VerificationResult captchaResult = verificationCodeStore.consumeCaptcha(key, captchaCode);
        if (captchaResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_EXPIRED);
        }
        if (captchaResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }
    }

    private void checkLoginLock(User user) {
        if (user.getLoginFailTime() == null) {
            return;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
        if (user.getLoginFailTime().isAfter(now)) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_LOCKED);
        }
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    private void increaseLoginFailCount(User user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        user.setLoginFailCount(failCount + 1);

        if (failCount + 1 >= securityProperties.getLogin().getMaxFailCount()) {
            user.setLoginFailTime(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
                    .plusMinutes(securityProperties.getLogin().getLockMinutes()));
        }

        userMapper.updateById(user);
    }

    private void resetLoginFailCount(User user) {
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            user.setLoginFailCount(0);
            user.setLoginFailTime(null);
            userMapper.updateById(user);
        }
    }

    private void upgradePasswordIfNeeded(User user, String rawPassword) {
        if (!passwordCipherService.needsUpgrade(user.getPassword())) {
            return;
        }
        user.setPassword(passwordCipherService.encode(rawPassword));
        userMapper.updateById(user);
    }

    private String buildDefaultAvatar(String username) {
        String seed = username == null || username.isBlank() ? "user" : username.trim();
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private User findUserByEmail(String email) {
        return userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getEmail, email)
                        .last("limit 1")
        );
    }

    /**
     * 登录支持账号、手机号、邮箱三选一匹配。
     */
    private User findUserByAccount(String account) {
        String trimmed = account.trim();

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getUsername, trimmed)
                        .last("limit 1")
        );
        if (user != null) {
            return user;
        }

        if (PHONE_PATTERN.matcher(trimmed).matches()) {
            user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getDeleted, 0)
                            .eq(User::getPhone, trimmed)
                            .last("limit 1")
            );
            if (user != null) {
                return user;
            }
        }

        String lower = trimmed.toLowerCase();
        if (EMAIL_PATTERN.matcher(lower).matches()) {
            user = findUserByEmail(lower);
        }
        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String buildResetPasswordMailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在执行忘记密码操作。\n"
                + "本次密码重置验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请忽略本邮件。";
    }

    private String buildLoginCodeMailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在通过邮箱验证码登录。\n"
                + "本次登录验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请尽快修改密码。";
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }
}
