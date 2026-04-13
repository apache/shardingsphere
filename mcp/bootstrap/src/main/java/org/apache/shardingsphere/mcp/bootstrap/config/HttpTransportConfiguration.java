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

package org.apache.shardingsphere.mcp.bootstrap.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;

import java.util.Objects;

/**
 * HTTP transport configuration.
 */
@RequiredArgsConstructor
@Getter
public final class HttpTransportConfiguration {
    
    private final boolean enabled;
    
    private final String bindHost;
    
    private final boolean allowRemoteAccess;
    
    private final String accessToken;
    
    private final int port;
    
    private final String endpointPath;
    
    /**
     * Validate HTTP transport configuration.
     */
    public void validate() {
        if (!enabled) {
            return;
        }
        boolean loopbackBinding = isLoopbackBinding();
        ShardingSpherePreconditions.checkState(allowRemoteAccess || loopbackBinding,
                () -> new IllegalArgumentException("Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
        ShardingSpherePreconditions.checkState(loopbackBinding || hasAccessToken(),
                () -> new IllegalArgumentException("Property `transport.http.accessToken` must not be blank when remote HTTP access is enabled."));
    }
    
    private boolean isLoopbackBinding() {
        return HttpTransportHostUtils.isLoopbackHost(bindHost);
    }
    
    private boolean hasAccessToken() {
        return !Objects.toString(accessToken, "").trim().isEmpty();
    }
}
