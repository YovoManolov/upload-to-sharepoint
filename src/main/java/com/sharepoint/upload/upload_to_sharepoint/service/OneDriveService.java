package com.sharepoint.upload.upload_to_sharepoint.service;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OneDriveService {

    @Value("${onedrive.auth.url}")
    private String authUrl;

    @Value("${onedrive.token.url}")
    private String tokenUrl;

    @Value("${onedrive.upload.url}")
    private String uploadUrl;

    @Value("${onedrive.client.id}")
    private String clientId;

    @Value("${onedrive.client.secret}")
    private String clientSecret;

    @Value("${onedrive.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Step 1: Generate the Microsoft Login URL for Authorization
     */
    public String getAuthCodeUrl() {
        return authUrl + "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=Files.ReadWrite" +
                "&state=randomState";
    }

    /**
     * Step 2: Exchange Authorization Code for Access Token
     */
    public String getAccessToken(String authCode) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", redirectUri);
        params.put("code", authCode);
        params.put("scope", "Files.ReadWrite");

        Map<String, Object> response = restTemplate.postForObject(tokenUrl, params, Map.class);
        return response != null ? (String) response.get("access_token") : null;
    }

    /**
     * Step 3: Upload File to OneDrive
     */
    public String uploadFile(String filePath, String accessToken, String contentType) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return "File not found!";
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String dynamicUploadUrl = uploadUrl + file.getName() + ":/content";

            HttpPut request = new HttpPut(dynamicUploadUrl);
            request.setHeader("Authorization", "Bearer " + accessToken);
            request.setHeader("Content-Type", contentType);
            request.setHeader("Accept", "application/json");

            request.setEntity(new FileEntity(file, ContentType.parse(contentType)));

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                return statusCode == 200 ? "Upload successful!" : "Upload failed! Error: " + responseBody;
            });
        }
    }
}