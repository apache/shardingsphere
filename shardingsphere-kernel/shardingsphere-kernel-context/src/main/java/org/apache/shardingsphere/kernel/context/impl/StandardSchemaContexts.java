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

package org.apache.shardingsphere.kernel.context.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Standard schema contexts.
 */
@RequiredArgsConstructor
@Getter
public final class StandardSchemaContexts implements SchemaContexts {
    
    private final Map<String, SchemaContext> schemaContexts;
    
    private final Authentication authentication;
    
    private final ConfigurationProperties props;
    
    private final DatabaseType databaseType;
    
    private final boolean isCircuitBreak;
    
    public StandardSchemaContexts() {
        // TODO MySQLDatabaseType is invalid because it can not update again
        this(new HashMap<>(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType(), false);
    }
    
    public StandardSchemaContexts(final Map<String, SchemaContext> schemaContexts, final Authentication authentication, final ConfigurationProperties props, final DatabaseType databaseType) {
        this(schemaContexts, authentication, props, databaseType, false);
    }
    
    @Override
    public SchemaContext getDefaultSchemaContext() {
        return schemaContexts.get(DefaultSchema.LOGIC_NAME);
    }
    
    @Override
    public void close() {
        schemaContexts.values().forEach(each -> each.getRuntimeContext().getExecutorKernel().close());
    }
}
