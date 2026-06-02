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

package org.apache.shardingsphere.mcp.support.database.tool.request;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * Proxy preflight validation request.
 */
@Getter
public final class ProxyPreflightValidationRequest {
    
    private final String databaseType;
    
    private final String jdbcUrl;
    
    private final String username;
    
    private final String password;
    
    private final String driverClassName;
    
    private final String database;
    
    public ProxyPreflightValidationRequest(final String databaseType, final String jdbcUrl, final String username, final String password, final String driverClassName, final String database) {
        this.databaseType = Objects.toString(databaseType, "");
        this.jdbcUrl = Objects.toString(jdbcUrl, "");
        this.username = Objects.toString(username, "");
        this.password = Objects.toString(password, "");
        this.driverClassName = Objects.toString(driverClassName, "");
        this.database = Objects.toString(database, "");
    }
    
    /**
     * Create request from MCP tool arguments.
     *
     * @param arguments tool arguments
     * @return request
     */
    public static ProxyPreflightValidationRequest from(final Map<String, Object> arguments) {
        return new ProxyPreflightValidationRequest(
                getRawString(arguments, "databaseType"),
                getRawString(arguments, "jdbcUrl"),
                getRawString(arguments, "username"),
                getRawString(arguments, "password"),
                getRawString(arguments, "driverClassName"),
                getRawString(arguments, "database"));
    }
    
    private static String getRawString(final Map<String, Object> arguments, final String fieldName) {
        return Objects.toString(arguments.get(fieldName), "");
    }
}
