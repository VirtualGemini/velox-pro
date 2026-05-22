package com.velox.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "角色查询参数")
public class RoleQuery {

    @Schema(description = "当前页", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "20")
    private Long size = 20L;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色类型：0系统角色，1自定义角色")
    private Integer type;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "开始时间", example = "2025-05-01")
    private String startTime;

    @Schema(description = "结束时间", example = "2025-05-31")
    private String endTime;

    @Schema(description = "创建开始时间", example = "2026-05-22 10:00:00")
    private String createTimeStart;

    @Schema(description = "创建结束时间", example = "2026-05-22 18:00:00")
    private String createTimeEnd;

    @Schema(description = "编辑开始时间", example = "2026-05-22 10:00:00")
    private String updateTimeStart;

    @Schema(description = "编辑结束时间", example = "2026-05-22 18:00:00")
    private String updateTimeEnd;

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getUpdateTimeStart() {
        return updateTimeStart;
    }

    public void setUpdateTimeStart(String updateTimeStart) {
        this.updateTimeStart = updateTimeStart;
    }

    public String getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(String updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }
}
