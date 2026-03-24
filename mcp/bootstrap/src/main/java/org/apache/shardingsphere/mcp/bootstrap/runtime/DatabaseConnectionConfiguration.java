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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import lombok.Getter;

import java.util.Objects;

/**
 * JDBC connection configuration for one logical database.
 */
@Getter
public final class DatabaseConnectionConfiguration {
    
    private final String database;
    
    private final String databaseType;
    
    private final String jdbcUrl;
    
    private final String username;
    
    private final String password;
    
    private final String driverClassName;
    
    private final boolean legacySupportsCrossSchemaSqlConfigured;
    
    private final boolean legacySupportsCrossSchemaSql;
    
    private final boolean legacySupportsExplainAnalyzeConfigured;
    
    private final boolean legacySupportsExplainAnalyze;
    
    /**
     * Construct one JDBC connection configuration.
     *
     * @param database logical database name
     * @param databaseType database type
     * @param jdbcUrl JDBC URL
     * @param username username
     * @param password password
     * @param driverClassName driver class name
     * @param legacySupportsCrossSchemaSqlConfigured legacy cross-schema SQL override configured flag
     * @param legacySupportsCrossSchemaSql legacy cross-schema SQL override flag
     * @param legacySupportsExplainAnalyzeConfigured legacy explain analyze override configured flag
     * @param legacySupportsExplainAnalyze legacy explain analyze override flag
     */
    public DatabaseConnectionConfiguration(final String database, final String databaseType, final String jdbcUrl, final String username,
                                           final String password, final String driverClassName, final boolean legacySupportsCrossSchemaSqlConfigured,
                                           final boolean legacySupportsCrossSchemaSql, final boolean legacySupportsExplainAnalyzeConfigured,
                                           final boolean legacySupportsExplainAnalyze) {
        this.database = Objects.requireNonNull(database, "database cannot be null");
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.password = Objects.requireNonNull(password, "password cannot be null");
        this.driverClassName = Objects.requireNonNull(driverClassName, "driverClassName cannot be null");
        this.legacySupportsCrossSchemaSqlConfigured = legacySupportsCrossSchemaSqlConfigured;
        this.legacySupportsCrossSchemaSql = legacySupportsCrossSchemaSql;
        this.legacySupportsExplainAnalyzeConfigured = legacySupportsExplainAnalyzeConfigured;
        this.legacySupportsExplainAnalyze = legacySupportsExplainAnalyze;
    }
}
