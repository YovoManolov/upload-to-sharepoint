package com.sharepoint.upload.upload_to_sharepoint.controller;

import com.sharepoint.upload.upload_to_sharepoint.service.OneDriveService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/onedrive")
public class OneDriveController {

    private final OneDriveService oneDriveService;

    public OneDriveController(OneDriveService service) {
        this.oneDriveService = service;
    }

    /**
     * Step 3: Upload file using access token
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam String filePath,
                             @RequestParam @Schema(description = "File content type",
                                     allowableValues = {"application/xml", "application/pdf", "text/csv"})
                             String contentType,
                             @RequestParam String accessToken) throws IOException {
        return oneDriveService.uploadFile(filePath, accessToken, contentType);
    }
}
