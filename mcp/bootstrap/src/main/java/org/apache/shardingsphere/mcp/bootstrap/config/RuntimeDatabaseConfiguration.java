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
 * Runtime database configuration for one logical database binding.
 */
@Getter
public final class RuntimeDatabaseConfiguration {
    
    private final String databaseType;
    
    private final String jdbcUrl;
    
    private final String username;
    
    private final String password;
    
    private final String driverClassName;
    
    private final String schemaPattern;
    
    private final String defaultSchema;
    
    private final boolean supportsCrossSchemaSql;
    
    private final boolean supportsExplainAnalyze;
    
    /**
     * Construct a runtime database configuration.
     *
     * @param databaseType database type
     * @param jdbcUrl JDBC URL
     * @param username username
     * @param password password
     * @param driverClassName driver class name
     * @param schemaPattern schema pattern
     * @param defaultSchema default schema
     * @param supportsCrossSchemaSql cross-schema SQL support flag
     * @param supportsExplainAnalyze explain analyze support flag
     */
    public RuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl, final String username, final String password,
                                        final String driverClassName, final String schemaPattern, final String defaultSchema,
                                        final boolean supportsCrossSchemaSql, final boolean supportsExplainAnalyze) {
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.password = Objects.requireNonNull(password, "password cannot be null");
        this.driverClassName = Objects.requireNonNull(driverClassName, "driverClassName cannot be null");
        this.schemaPattern = Objects.requireNonNull(schemaPattern, "schemaPattern cannot be null");
        this.defaultSchema = Objects.requireNonNull(defaultSchema, "defaultSchema cannot be null");
        this.supportsCrossSchemaSql = supportsCrossSchemaSql;
        this.supportsExplainAnalyze = supportsExplainAnalyze;
    }
}
