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
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;

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
    
    private final Map<CaseInsensitiveIdentifier, String> mapping;
    
    public TableAndSchemaNameMapper(final Map<String, String> tableSchemaMap) {
        mapping = null == tableSchemaMap ? Collections.emptyMap() : getLogicTableNameMap(tableSchemaMap);
    }
    
    public TableAndSchemaNameMapper(final Collection<String> tableNames) {
        Map<String, String> tableNameSchemaMap = tableNames.stream().map(each -> each.split("\\.")).filter(split -> split.length > 1).collect(Collectors.toMap(split -> split[1], split -> split[0]));
        mapping = getLogicTableNameMap(tableNameSchemaMap);
    }
    
    private Map<CaseInsensitiveIdentifier, String> getLogicTableNameMap(final Map<String, String> tableSchemaMap) {
        Map<CaseInsensitiveIdentifier, String> result = new HashMap<>(tableSchemaMap.size(), 1F);
        for (Entry<String, String> entry : tableSchemaMap.entrySet()) {
            String tableName = entry.getKey();
            String schemaName = entry.getValue();
            if (null != schemaName) {
                result.put(new CaseInsensitiveIdentifier(tableName), schemaName);
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
        return mapping.get(new CaseInsensitiveIdentifier(logicTableName));
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public String getSchemaName(final CaseInsensitiveIdentifier logicTableName) {
        return mapping.get(logicTableName);
    }
}
