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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper;

import lombok.ToString;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Table and schema name mapper.
 */
@ToString
public final class TableAndSchemaNameMapper {
    
    private final Map<ShardingSphereIdentifier, String> mapping;
    
    public TableAndSchemaNameMapper(final Map<String, String> tableSchemaMap) {
        mapping = null == tableSchemaMap ? Collections.emptyMap() : getLogicTableNameMap(tableSchemaMap);
    }
    
    public TableAndSchemaNameMapper(final Collection<String> tableNames) {
        Map<String, String> tableNameSchemaMap = tableNames.stream().map(each -> each.split("\\.")).filter(split -> split.length > 1).collect(Collectors.toMap(split -> split[1], split -> split[0]));
        mapping = getLogicTableNameMap(tableNameSchemaMap);
    }
    
    private Map<ShardingSphereIdentifier, String> getLogicTableNameMap(final Map<String, String> tableSchemaMap) {
        Map<ShardingSphereIdentifier, String> result = new HashMap<>(tableSchemaMap.size(), 1F);
        for (Entry<String, String> entry : tableSchemaMap.entrySet()) {
            String tableName = entry.getKey();
            String schemaName = entry.getValue();
            if (null != schemaName) {
                result.put(new ShardingSphereIdentifier(tableName), schemaName);
            }
        }
        return result;
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public String getSchemaName(final String logicTableName) {
        return mapping.get(new ShardingSphereIdentifier(logicTableName));
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public String getSchemaName(final ShardingSphereIdentifier logicTableName) {
        return mapping.get(logicTableName);
    }
    
    /**
     * Get qualified tables.
     *
     * @return qualified tables
     */
    public Collection<QualifiedTable> getQualifiedTables() {
        return mapping.entrySet().stream().map(entry -> new QualifiedTable(entry.getValue(), entry.getKey().getValue())).collect(Collectors.toList());
    }
}
