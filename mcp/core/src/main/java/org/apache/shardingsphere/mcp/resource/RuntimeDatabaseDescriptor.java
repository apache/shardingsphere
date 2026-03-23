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

package org.apache.shardingsphere.mcp.resource;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.SupportedObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Runtime metadata facts for one logical database.
 */
@Getter
public final class RuntimeDatabaseDescriptor {
    
    private final String database;
    
    private final String databaseType;
    
    private final Set<SupportedObjectType> supportedObjectTypes;
    
    private final String defaultSchema;
    
    private final boolean supportsCrossSchemaSql;
    
    private final boolean supportsExplainAnalyze;
    
    /**
     * Construct one runtime database descriptor.
     *
     * @param database logical database name
     * @param databaseType database type
     * @param supportedObjectTypes supported object types
     * @param defaultSchema default schema
     * @param supportsCrossSchemaSql cross-schema SQL support flag
     * @param supportsExplainAnalyze explain analyze support flag
     */
    public RuntimeDatabaseDescriptor(final String database, final String databaseType, final Collection<SupportedObjectType> supportedObjectTypes,
                                     final String defaultSchema, final boolean supportsCrossSchemaSql, final boolean supportsExplainAnalyze) {
        this.database = Objects.requireNonNull(database, "database cannot be null");
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
        this.supportedObjectTypes = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(supportedObjectTypes, "supportedObjectTypes cannot be null")));
        this.defaultSchema = Objects.requireNonNull(defaultSchema, "defaultSchema cannot be null");
        this.supportsCrossSchemaSql = supportsCrossSchemaSql;
        this.supportsExplainAnalyze = supportsExplainAnalyze;
    }
}
