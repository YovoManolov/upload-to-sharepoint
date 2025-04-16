package com.sharepoint.upload.upload_to_sharepoint.service;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Service
public class OneDriveService {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveService.class);

    private final TokenService tokenService;

    @Value("${onedrive.upload.url}")
    private String uploadUrl;

    @Autowired
    public OneDriveService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String uploadFile(String filePath, String contentType) throws IOException {
        File file = new File(filePath);
        if (!isValidContentType(contentType)) {
            return "Invalid content type. Allowed types: application/xml, application/pdf, text/csv";
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String dynamicUploadUrl = uploadUrl + file.getName() + ":/content";
            logger.info("Upload URL: {}", dynamicUploadUrl);

            HttpPut request = new HttpPut(dynamicUploadUrl);
            request.setHeader("Content-Type", contentType);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            request.setHeader("Prefer", "respond-async");

            logger.info("Calling TokenService to fetch token...");
            String token = tokenService.getAccessToken();
            if (token == null || token.isEmpty()) {
                logger.error("Failed to retrieve access token.");
                return "Failed to retrieve access token.";
            }
            request.setHeader("Authorization", "Bearer " + token);

            request.setEntity(new FileEntity(file, ContentType.parse(contentType)));

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("Response Code: {}", statusCode);
                logger.info("Response Body: {}", responseBody);
                return statusCode == 200 ? "Upload successful!" : "Upload failed! Error: " + responseBody;
            });
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("application/xml") ||
                contentType.equals("application/pdf") ||
                contentType.equals("text/csv");
    }
}
