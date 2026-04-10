package com.drama.tracker.controller;

import com.drama.tracker.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传", description = "图片上传接口")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String UPLOAD_DIR = "/tmp/uploads/";

    /**
     * 上传图片（MultipartFile）。
     */
    @Operation(summary = "上传图片文件")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("请选择文件");
        if (file.getSize() > MAX_SIZE) return Result.fail("文件大小不能超过5MB");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只能上传图片文件");
        }

        try {
            // 转 Base64 Data URL
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64;
            return Result.success(dataUrl);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.fail("上传失败");
        }
    }

    /**
     * 上传 Base64 图片。
     */
    @Operation(summary = "上传Base64图片")
    @PostMapping("/base64")
    public Result<String> uploadBase64(@RequestBody java.util.Map<String, String> body) {
        String base64Data = body.get("image");
        if (base64Data == null || base64Data.isEmpty()) return Result.fail("图片数据为空");

        // 如果已经是 data URL 格式，直接返回
        if (base64Data.startsWith("data:image/")) {
            return Result.success(base64Data);
        }

        // 添加前缀
        return Result.success("data:image/jpeg;base64," + base64Data);
    }
}
