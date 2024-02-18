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

package org.apache.shardingsphere.infra.database.core.type;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Database type registry.
 */
public final class DatabaseTypeRegistry {
    
    private static final Map<DatabaseType, DialectDatabaseMetaData> REGISTERED_META_DATA = new ConcurrentHashMap<>();
    
    private final DatabaseType databaseType;
    
    @Getter
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    public DatabaseTypeRegistry(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        if (!REGISTERED_META_DATA.containsKey(databaseType)) {
            REGISTERED_META_DATA.put(databaseType, DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType));
        }
        dialectDatabaseMetaData = REGISTERED_META_DATA.get(databaseType);
    }
    
    /**
     * Get all branch database types.
     * 
     * @return all branch database types
     */
    public Collection<DatabaseType> getAllBranchDatabaseTypes() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)
                .stream().filter(each -> each.getTrunkDatabaseType().map(optional -> optional == databaseType).orElse(false)).collect(Collectors.toList());
    }
    
    /**
     * Get default schema name.
     *
     * @param databaseName database name
     * @return default schema name
     */
    public String getDefaultSchemaName(final String databaseName) {
        return dialectDatabaseMetaData.getDefaultSchema().orElseGet(() -> null == databaseName ? null : databaseName.toLowerCase());
    }
}
