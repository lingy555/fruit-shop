package com.lingnan.fruitshop.controller;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.common.exception.BizException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BizException.badRequest("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);

        String filename = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank()) {
            filename = filename + "." + ext;
        }

        try {
            Path dir = Paths.get(UPLOAD_DIR);
            Files.createDirectories(dir);

            Path dest = dir.resolve(filename);
            file.transferTo(dest);

            String url = "/uploads/" + filename;
            return ApiResponse.success(Map.of("url", url));
        } catch (IOException e) {
            throw BizException.serverError("文件保存失败");
        }
    }
}
