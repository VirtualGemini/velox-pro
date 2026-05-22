package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送登录验证码请求")
public class LoginCodeSendCommand {

    @Schema(description = "登录方式：email / phone", example = "email")
    @NotBlank(message = "登录方式不能为空")
    private String type;

    @Schema(description = "邮箱或手机号", example = "user@example.com")
    @NotBlank(message = "邮箱/手机号不能为空")
    private String target;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
