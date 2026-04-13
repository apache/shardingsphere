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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class AccessTokenSecurityValidator implements ServerTransportSecurityValidator {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized.";
    
    private final String accessToken;
    
    private AccessTokenSecurityValidator(final String accessToken) {
        this.accessToken = accessToken;
    }
    
    static ServerTransportSecurityValidator create(final String accessToken) {
        String actualAccessToken = Objects.toString(accessToken, "").trim();
        return actualAccessToken.isEmpty() ? NOOP : new AccessTokenSecurityValidator(actualAccessToken);
    }
    
    @Override
    public void validateHeaders(final Map<String, List<String>> headers) throws ServerTransportSecurityException {
        String authorization = HttpTransportSecurityHeaderUtils.getFirstHeaderValue(headers, AUTHORIZATION_HEADER);
        String[] authorizationSegments = authorization.isEmpty() ? new String[0] : authorization.split("\\s+", 2);
        if (2 == authorizationSegments.length && "Bearer".equalsIgnoreCase(authorizationSegments[0]) && accessToken.equals(authorizationSegments[1].trim())) {
            return;
        }
        throw new ServerTransportSecurityException(401, UNAUTHORIZED_MESSAGE);
    }
}
