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
 * HTTP server configuration.
 */
@Getter
public final class HttpServerConfiguration {
    
    private final String bindHost;
    
    private final int port;
    
    private final String endpointPath;
    
    /**
     * Construct one HTTP server configuration.
     *
     * @param bindHost bind host
     * @param port bind port
     * @param endpointPath endpoint path
     */
    public HttpServerConfiguration(final String bindHost, final int port, final String endpointPath) {
        this.bindHost = Objects.requireNonNull(bindHost, "bindHost cannot be null");
        this.port = port;
        this.endpointPath = Objects.requireNonNull(endpointPath, "endpointPath cannot be null");
    }
}
