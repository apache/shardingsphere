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

package org.apache.shardingsphere.database.connector.core.metadata.database.system;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Collections;
import java.util.Optional;

/**
 * System table.
 */
@RequiredArgsConstructor
public final class SystemTable {
    
    private final DatabaseType databaseType;
    
    /**
     * Judge whether supported system table.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return is supported system table or not
     */
    public boolean isSupportedSystemTable(final String schemaName, final String tableName) {
        if ("shardingsphere".equals(schemaName) && "cluster_information".equals(tableName)) {
            return true;
        }
        Optional<DialectKernelSupportedSystemTable> kernelSupportedSystemTable = DatabaseTypedSPILoader.findService(DialectKernelSupportedSystemTable.class, databaseType);
        return kernelSupportedSystemTable.map(optional -> optional.getSchemaAndTablesMap().getOrDefault(schemaName, Collections.emptySet()).contains(tableName)).orElse(false);
    }
}
