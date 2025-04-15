package com.sharepoint.upload.upload_to_sharepoint.service;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class OneDriveService {

    @Value("${onedrive.token}")
    private String accessToken;

    @Value("${onedrive.upload.url}")
    private String uploadUrl;

    public String uploadFile(String filePath, String contentType) throws IOException {
        File file = new File(filePath);

        // Validate Content Type
        if (!isValidContentType(contentType)) {
            return "Invalid content type. Allowed types: application/xml, application/pdf, text/csv";
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(uploadUrl);
            request.setHeader("Authorization", "Bearer " + accessToken);
            request.setHeader("Content-Type", contentType);

            request.setEntity(new FileEntity(file, ContentType.parse(contentType)));

            return client.execute(request, response -> response.getCode() == 200 ? "Upload successful!" : "Upload failed!");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("application/xml") ||
                contentType.equals("application/pdf") ||
                contentType.equals("text/csv");
    }

}
