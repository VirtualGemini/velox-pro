package com.velox.module.system.user.security.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindProofDTO;
import com.velox.module.system.user.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.user.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.user.security.dto.MfaTotpProvisionDTO;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;
import com.velox.module.system.user.security.service.AccountSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "账号安全", description = "账号安全设置相关接口")
@RestController
@RequestMapping("/user/security")
public class AccountSecurityController {

    private final AccountSecurityService accountSecurityService;

    public AccountSecurityController(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }

    @Operation(summary = "查询账号安全状态")
    @GetMapping("/status")
    @RequirePermission("system:user-center:security-query")
    public Result<SecurityStatusDTO> getStatus() {
        return Result.ok(accountSecurityService.getStatus());
    }

    @Operation(summary = "发送邮箱换绑前置验证验证码")
    @PostMapping("/email/rebind/proof/send-code")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Void> sendEmailRebindProofCode() {
        accountSecurityService.sendEmailRebindProofCode();
        return Result.ok();
    }

    @Operation(summary = "校验邮箱换绑前置验证")
    @PostMapping("/email/rebind/proof/verify")
    @RequirePermission("system:user-center:email-rebind")
    public Result<EmailRebindProofDTO> verifyEmailRebindProof(
            @Valid @RequestBody EmailRebindProofVerifyCommand command) {
        return Result.ok(accountSecurityService.verifyEmailRebindProof(command));
    }

    @Operation(summary = "发送邮箱换绑验证码")
    @PostMapping("/email/rebind/send-code")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Void> sendEmailRebindCode(@Valid @RequestBody EmailRebindSendCodeCommand command) {
        accountSecurityService.sendEmailRebindCode(command);
        return Result.ok();
    }

    @Operation(summary = "提交邮箱换绑")
    @PutMapping("/email/rebind")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Boolean> rebindEmail(@Valid @RequestBody EmailRebindCommand command) {
        return Result.ok(accountSecurityService.rebindEmail(command));
    }

    @Operation(summary = "更新登录方式")
    @PutMapping("/login-methods")
    @RequirePermission("system:user-center:security-update")
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody LoginMethodsUpdateCommand command) {
        return Result.ok(accountSecurityService.updateLoginMethods(command));
    }

    @Operation(summary = "发送邮箱 MFA 验证码")
    @PostMapping("/mfa/email/send-code")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Void> sendMfaEmailCode() {
        accountSecurityService.sendMfaEmailCode();
        return Result.ok();
    }

    @Operation(summary = "开启或关闭邮箱二次验证")
    @PutMapping("/mfa/email")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> updateMfaEmail(@Valid @RequestBody MfaEmailUpdateCommand command) {
        return Result.ok(accountSecurityService.updateMfaEmail(command));
    }

    @Operation(summary = "拉取 TOTP 绑定材料（密钥与二维码 URI）")
    @PostMapping("/mfa/totp/provision")
    @RequirePermission("system:user-center:mfa-update")
    public Result<MfaTotpProvisionDTO> provisionMfaTotp() {
        return Result.ok(accountSecurityService.provisionMfaTotp());
    }

    @Operation(summary = "完成 TOTP 绑定")
    @PutMapping("/mfa/totp/enable")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> enableMfaTotp(@Valid @RequestBody MfaTotpEnableCommand command) {
        return Result.ok(accountSecurityService.enableMfaTotp(command));
    }

    @Operation(summary = "解绑 TOTP")
    @PutMapping("/mfa/totp/disable")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> disableMfaTotp(@Valid @RequestBody MfaTotpDisableCommand command) {
        return Result.ok(accountSecurityService.disableMfaTotp(command));
    }
}
