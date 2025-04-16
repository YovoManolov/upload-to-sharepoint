package com.sharepoint.upload.upload_to_sharepoint.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${onedrive.tenant.id}")
    private String tenantId;

    @Value("${onedrive.client.id}")
    private String clientId;

    @Value("${onedrive.client.secret}")
    private String clientSecret;

    private static final String TOKEN_URL_TEMPLATE = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    private static final String SCOPE = "https://graph.microsoft.com/.default";

    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = String.format(TOKEN_URL_TEMPLATE, tenantId);

        logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++");
        logger.debug("Token URL: {}", tokenUrl);
        logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++");

        Map<String, String> requestBody = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "scope", SCOPE,
                "grant_type", "client_credentials"
        );

        logger.debug("Request Body: {}", requestBody);

        final Map response = restTemplate.postForObject(tokenUrl, requestBody, Map.class);

        String accessToken = (String) response.get("access_token");
        if (accessToken == null) {
            logger.info("*******************************");
            logger.info("Access token is missing in response: {}", response);
        } else {
            logger.info("*******************************");
            logger.info("Successfully received access token.");
        }

        return accessToken;
    }
}