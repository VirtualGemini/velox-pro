# Velox TOTP Spring Boot Starter

Velox 的通用 TOTP 能力 starter。

## 能力边界

本模块提供：

- secret 生成
- TOTP 校验
- otpauth URI 生成
- provisioning 模型
- `velox.totp.enabled=false` 时的 disabled/noop 行为

本模块不提供：

- MFA 开通流程
- MFA 挑战流程
- 恢复码流程
- 账号持久化
- 邮箱换绑证明流程

这些业务编排统一归属 `velox-system`。

## 配置

```yaml
velox:
  totp:
    enabled: true
    issuer: ${velox.name:Velox}
    digits: 6
    period-seconds: 30
    algorithm: SHA1
    secret-size-bytes: 20
    verify-window-steps: 1
```

## 禁用行为

当 `velox.totp.enabled=false` 时，starter 注册 `DisabledTotpService`：

- `isEnabled()` 返回 `false`
- provision / verify 会拒绝使用
- 业务模块仍可注入 `TotpService`，并自行决定降级策略

## API 示例

```java
TotpProvisioning provisioning = totpService.provision("user@example.com");
TotpVerifyResult result = totpService.verify(secret, code);
```
