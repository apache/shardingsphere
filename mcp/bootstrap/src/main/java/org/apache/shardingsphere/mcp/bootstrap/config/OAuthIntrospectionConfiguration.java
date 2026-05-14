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

import java.util.Objects;

/**
 * OAuth introspection configuration.
 */
@Getter
public final class OAuthIntrospectionConfiguration {
    
    private final String endpoint;
    
    private final String clientId;
    
    private final String clientSecret;
    
    private final String expectedIssuer;
    
    private final long cacheTtlMillis;
    
    public OAuthIntrospectionConfiguration() {
        this("", "", "", "", 0L);
    }
    
    public OAuthIntrospectionConfiguration(final String endpoint, final String clientId, final String clientSecret, final String expectedIssuer, final long cacheTtlMillis) {
        this.endpoint = Objects.toString(endpoint, "").trim();
        this.clientId = Objects.toString(clientId, "").trim();
        this.clientSecret = Objects.toString(clientSecret, "").trim();
        this.expectedIssuer = Objects.toString(expectedIssuer, "").trim();
        this.cacheTtlMillis = cacheTtlMillis;
    }
    
    /**
     * Judge whether OAuth introspection is enabled.
     *
     * @return OAuth introspection is enabled or not
     */
    public boolean isEnabled() {
        return !endpoint.isEmpty();
    }
}
