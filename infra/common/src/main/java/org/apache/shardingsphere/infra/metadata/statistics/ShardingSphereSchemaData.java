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

package org.apache.shardingsphere.infra.metadata.statistics;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere schema data.
 */
@Getter
public final class ShardingSphereSchemaData {
    
    private final Map<String, ShardingSphereTableData> tableData = new LinkedHashMap<>();
    
    /**
     * Get ShardingSphere table meta data via table name.
     *
     * @param tableName tableName table name
     * @return ShardingSphere table data
     */
    public ShardingSphereTableData getTable(final String tableName) {
        return tableData.get(tableName.toLowerCase());
    }
    
    /**
     * Add ShardingSphere table data.
     *
     * @param tableName table name
     * @param table ShardingSphere table data
     */
    public void putTable(final String tableName, final ShardingSphereTableData table) {
        tableData.put(tableName.toLowerCase(), table);
    }
    
    /**
     * Remove ShardingSphere table meta data.
     *
     * @param tableName table name
     */
    public void removeTable(final String tableName) {
        tableData.remove(tableName.toLowerCase());
    }
    
    /**
     * Judge contains ShardingSphere table from table metadata or not.
     *
     * @param tableName table name
     * @return contains ShardingSphere table from table metadata or not
     */
    public boolean containsTable(final String tableName) {
        return tableData.containsKey(tableName.toLowerCase());
    }
}
