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

package org.apache.shardingsphere.scaling.core.common.datasource;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta data manager.
 */
@RequiredArgsConstructor
public final class MetaDataManager {
    
    private final DataSource dataSource;
    
    private final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
    
    /**
     * Get table meta data by table name.
     *
     * @param tableName table name
     * @param databaseType database type
     * @return table meta data
     */
    public TableMetaData getTableMetaData(final String tableName, final DatabaseType databaseType) {
        if (!tableMetaDataMap.containsKey(tableName)) {
            try {
                TableMetaDataLoaderEngine.load(Collections.singletonList(new TableMetaDataLoaderMaterial(Collections.singletonList(tableName), dataSource)), databaseType)
                        .stream().findFirst().ifPresent(tableMetaData -> tableMetaDataMap.put(tableName, tableMetaData));
            } catch (final SQLException ex) {
                throw new RuntimeException(String.format("Load metaData for table %s failed", tableName), ex);
            }
        }
        return tableMetaDataMap.get(tableName);
    }
}
