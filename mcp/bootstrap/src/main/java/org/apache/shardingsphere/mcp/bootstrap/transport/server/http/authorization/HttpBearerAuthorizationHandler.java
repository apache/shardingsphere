/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * HTTP bearer authorization handler.
 */
public final class HttpBearerAuthorizationHandler {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    
    private static final String BEARER_SCHEME = "Bearer";
    
    private final String accessToken;
    
    private final String endpointPath;
    
    private final List<String> scopesSupported;
    
    private final boolean protectedResourceMetadataEnabled;
    
    private final OAuthTokenValidator oauthTokenValidator;
    
    public HttpBearerAuthorizationHandler(final HttpTransportConfiguration config) {
        accessToken = Objects.toString(config.getAccessToken(), "").trim();
        endpointPath = config.getEndpointPath();
        scopesSupported = config.getScopesSupported().stream().map(each -> Objects.toString(each, "").trim()).filter(each -> !each.isEmpty()).toList();
        protectedResourceMetadataEnabled = config.isProtectedResourceMetadataEnabled();
        oauthTokenValidator = config.getOauthIntrospection().isEnabled() ? new OAuthTokenValidator(config) : null;
    }
    
    /**
     * Authorize HTTP request.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @return authorized or not
     * @throws IOException IO exception
     */
    public boolean authorize(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (accessToken.isEmpty() && null == oauthTokenValidator) {
            return true;
        }
        String bearerToken = extractBearerToken(request);
        if (bearerToken.isEmpty()) {
            return sendUnauthorized(request, response, null == oauthTokenValidator ? "" : "invalid_token");
        }
        if (null == oauthTokenValidator) {
            return accessToken.equals(bearerToken) || sendUnauthorized(request, response, "");
        }
        OAuthTokenValidationResult validationResult = oauthTokenValidator.validate(bearerToken, request);
        return validationResult.isValid() || sendOAuthError(request, response, validationResult);
    }
    
    private String extractBearerToken(final HttpServletRequest request) {
        String authorization = Objects.toString(request.getHeader(AUTHORIZATION_HEADER), "").trim();
        if (authorization.length() <= BEARER_SCHEME.length() || !authorization.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())
                || !Character.isWhitespace(authorization.charAt(BEARER_SCHEME.length()))) {
            return "";
        }
        return authorization.substring(BEARER_SCHEME.length()).trim();
    }
    
    private boolean sendUnauthorized(final HttpServletRequest request, final HttpServletResponse response, final String error) throws IOException {
        response.setHeader(WWW_AUTHENTICATE_HEADER, createAuthenticateChallenge(request, error));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return false;
    }
    
    private boolean sendOAuthError(final HttpServletRequest request, final HttpServletResponse response, final OAuthTokenValidationResult validationResult) throws IOException {
        response.setHeader(WWW_AUTHENTICATE_HEADER, createAuthenticateChallenge(request, validationResult.getError()));
        response.sendError(validationResult.getStatusCode(), HttpServletResponse.SC_FORBIDDEN == validationResult.getStatusCode() ? "Forbidden" : "Unauthorized");
        return false;
    }
    
    private String createAuthenticateChallenge(final HttpServletRequest request, final String error) {
        List<String> parameters = new LinkedList<>();
        if (protectedResourceMetadataEnabled) {
            parameters.add("resource_metadata=\"" + HttpAuthorizationUriUtils.createAbsoluteUri(request,
                    OAuthProtectedResourceMetadataServlet.createEndpointWellKnownPath(endpointPath)) + "\"");
        }
        if (!error.isEmpty()) {
            parameters.add("error=\"" + error + "\"");
        }
        if (!scopesSupported.isEmpty()) {
            parameters.add("scope=\"" + String.join(" ", scopesSupported) + "\"");
        }
        return parameters.isEmpty() ? BEARER_SCHEME : BEARER_SCHEME + " " + String.join(", ", parameters);
    }
}
