package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "验证码登录请求")
public class CodeLoginCommand {

    @Schema(description = "登录方式：email / phone", example = "email")
    @NotBlank(message = "登录方式不能为空")
    private String type;

    @Schema(description = "邮箱或手机号", example = "user@example.com")
    @NotBlank(message = "邮箱/手机号不能为空")
    private String target;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
