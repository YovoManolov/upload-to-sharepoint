package com.sharepoint.upload.upload_to_sharepoint.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

@Service
public class TokenService {

    @Value("${onedrive.token.url}")
    private String tokenUrl;

    @Value("${onedrive.client.id}")
    private String clientId;

    @Value("${onedrive.tenant.id}")
    private String tenantId;

    @Value("${onedrive.pfx.path}")
    private String pfxPath;  // Path to `.pfx` file

    @Value("${onedrive.pfx.password}")
    private String pfxPassword;  // Password for `.pfx`

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Step 1: Generate a JWT `client_assertion` from `.pfx`
     */
    public String generateClientAssertion() throws Exception {
        PrivateKey privateKey = getPrivateKeyFromPFX();
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setIssuer(clientId)
                .setSubject(clientId)
                .setAudience(tokenUrl)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(600)))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
        return jwt;
    }

    /**
     * Step 2: Extract Private Key from `.pfx`
     */
    private PrivateKey getPrivateKeyFromPFX() throws Exception {
        FileInputStream fis = new FileInputStream(pfxPath);
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(fis, pfxPassword.toCharArray());
        fis.close();

        Enumeration<String> aliases = keystore.aliases();
        String alias = aliases.nextElement();
        return (PrivateKey) keystore.getKey(alias, pfxPassword.toCharArray());
    }

    /**
     * Step 3: Request an Access Token using Certificate Authentication
     */
    public String getAccessToken() throws Exception {
        String clientAssertion = generateClientAssertion();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_assertion", clientAssertion);
        params.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        params.add("grant_type", "client_credentials");
        params.add("scope", "https://graph.microsoft.com/.default");

        Map response = restTemplate.postForObject(tokenUrl, params, Map.class);
        return (String) response.get("access_token");
    }
}
