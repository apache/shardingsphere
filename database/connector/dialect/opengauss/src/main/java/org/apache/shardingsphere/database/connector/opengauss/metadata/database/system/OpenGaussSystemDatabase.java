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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.system;

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * System database of openGauss.
 */
public final class OpenGaussSystemDatabase implements DialectSystemDatabase {
    
    private static final Map<String, Collection<String>> SYSTEM_DATABASE_SCHEMA_MAP = new HashMap<>();
    
    private static final Collection<String> SYSTEM_SCHEMAS = Arrays.asList("information_schema", "pg_catalog",
            "blockchain", "cstore", "db4ai", "dbe_perf", "dbe_pldebugger", "gaussdb", "oracle", "pkg_service", "snapshot", "sqladvisor", "dbe_pldeveloper", "pg_toast", "pkg_util", "shardingsphere");
    
    static {
        SYSTEM_DATABASE_SCHEMA_MAP.put("postgres", SYSTEM_SCHEMAS);
    }
    
    @Override
    public Collection<String> getSystemDatabases() {
        return SYSTEM_DATABASE_SCHEMA_MAP.keySet();
    }
    
    @Override
    public Collection<String> getSystemSchemas(final String databaseName) {
        return SYSTEM_DATABASE_SCHEMA_MAP.getOrDefault(databaseName, Collections.emptyList());
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return SYSTEM_SCHEMAS;
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
