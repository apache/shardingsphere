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

import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectKernelSupportedSystemTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Kernel supported system table for openGauss.
 */
public final class OpenGaussKernelSupportedSystemTable implements DialectKernelSupportedSystemTable {
    
    @Override
    public Map<String, Collection<String>> getSchemaAndTablesMap() {
        Map<String, Collection<String>> result = new HashMap<>(15, 1F);
        result.put("information_schema", Collections.emptySet());
        result.put("pg_catalog", Arrays.asList("pg_class", "pg_namespace", "pg_database", "pg_roles", "pg_tables", "pg_tablespace"));
        result.put("blockchain", Collections.emptySet());
        result.put("cstore", Collections.emptySet());
        result.put("db4ai", Collections.emptySet());
        result.put("dbe_perf", Collections.emptySet());
        result.put("dbe_pldebugger", Collections.emptySet());
        result.put("gaussdb", Collections.emptySet());
        result.put("oracle", Collections.emptySet());
        result.put("pkg_service", Collections.emptySet());
        result.put("snapshot", Collections.emptySet());
        result.put("dbe_pldeveloper", Collections.emptySet());
        result.put("pg_toast", Collections.emptySet());
        result.put("pkg_util", Collections.emptySet());
        result.put("sqladvisor", Collections.emptySet());
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
