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

package org.apache.shardingsphere.infra.metadata.schema;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Rule schema meta data.
 */
@RequiredArgsConstructor
@Getter
public final class RuleSchemaMetaData {
    
    private final SchemaMetaData configuredSchemaMetaData;
    
    private final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap;
    
    /**
     * Get schema meta data.
     * 
     * @return schema meta data
     */
    public SchemaMetaData getSchemaMetaData() {
        SchemaMetaData result = new SchemaMetaData();
        unconfiguredSchemaMetaDataMap.values().stream().flatMap(tableNames -> tableNames.stream()).forEach(tableName -> result.put(tableName, new TableMetaData()));
        result.merge(configuredSchemaMetaData);
        return result;
    }
    
    /**
     * Get all table names.
     *
     * @return all table names
     */
    public Collection<String> getAllTableNames() {
        Collection<String> result = new LinkedList<>(configuredSchemaMetaData.getAllTableNames());
        for (Collection<String> each : unconfiguredSchemaMetaDataMap.values()) {
            result.addAll(each);
        }
        return result;
    }
}
