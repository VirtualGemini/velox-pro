package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 文件配置分页 Request VO")
public class FileConfigPageReqVO {

    @Schema(description = "配置名")
    private String name;

    @Schema(description = "存储器")
    private Integer storage;

    @Schema(description = "启用状态")
    private Integer enabled;

    @Schema(description = "创建开始时间", example = "2026-05-22 10:00:00")
    private String createTimeStart;

    @Schema(description = "创建结束时间", example = "2026-05-22 18:00:00")
    private String createTimeEnd;

    @Schema(description = "编辑开始时间", example = "2026-05-22 10:00:00")
    private String updateTimeStart;

    @Schema(description = "编辑结束时间", example = "2026-05-22 18:00:00")
    private String updateTimeEnd;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
