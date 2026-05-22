package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 文件 Response VO")
public class FileRespVO {

    @Schema(description = "文件编号")
    private String id;

    @Schema(description = "配置编号")
    private String configId;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件路径")
    private String path;

    @Schema(description = "文件 URL")
    private String url;

    @Schema(description = "文件类型")
    private String type;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "上传时间")
    private String uploadTime;

    @Schema(description = "创建人")
    private String createBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
