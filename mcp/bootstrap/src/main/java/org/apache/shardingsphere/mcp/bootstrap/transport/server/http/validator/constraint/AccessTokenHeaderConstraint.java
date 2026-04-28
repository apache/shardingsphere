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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.HttpTransportSecurityHeaderUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.TransportHeaderConstraintException;

import java.util.List;
import java.util.Map;

/**
 * Access token header constraint.
 */
@RequiredArgsConstructor
public final class AccessTokenHeaderConstraint implements TransportHeaderConstraint {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    private static final String BEARER_AUTH_SCHEME = "Bearer";
    
    private final String accessToken;
    
    @Override
    public void validate(final Map<String, List<String>> headers) throws TransportHeaderConstraintException {
        String authorization = HttpTransportSecurityHeaderUtils.getFirstHeaderValue(headers, AUTHORIZATION_HEADER);
        String[] authorizationSegments = authorization.isEmpty() ? new String[0] : authorization.split("\\s+", 2);
        ShardingSpherePreconditions.checkState(2 == authorizationSegments.length
                        && BEARER_AUTH_SCHEME.equalsIgnoreCase(authorizationSegments[0]) && accessToken.equals(authorizationSegments[1].trim()),
                () -> new TransportHeaderConstraintException(401, "Unauthorized."));
    }
}
