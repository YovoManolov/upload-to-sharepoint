package com.sharepoint.upload.upload_to_sharepoint.controller;

import com.sharepoint.upload.upload_to_sharepoint.service.OneDriveService;
import com.sharepoint.upload.upload_to_sharepoint.service.TokenService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/onedrive")
public class OneDriveController {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveController.class);


    private final OneDriveService oneDriveService;

    private final TokenService tokenService;

    public OneDriveController(OneDriveService service, TokenService tokenService) {
        this.oneDriveService = service;
        this.tokenService = tokenService;
    }

    /**
     * Endpoint: Request an Access Token using Certificate Authentication
     */
    @GetMapping("/getAccessToken")
    public ResponseEntity<String> requestAccessToken() {
        try {
            String accessToken = tokenService.getAccessToken();
            return ResponseEntity.ok(accessToken);
        } catch (Exception e) {
            logger.error("Error retrieving access token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving token: " + e.getMessage());
        }
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
