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

package org.apache.shardingsphere.infra.metadata.model.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.model.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.schema.model.table.TableMetaData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Rule schema meta data.
 */
public final class RuleSchemaMetaData {
    
    @Getter
    private final SchemaMetaData configuredSchemaMetaData;
    
    @Getter
    private final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap;
    
    private final SchemaMetaData allSchemaMetaData;
    
    public RuleSchemaMetaData(final SchemaMetaData configuredSchemaMetaData, final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap) {
        this.configuredSchemaMetaData = configuredSchemaMetaData;
        this.unconfiguredSchemaMetaDataMap = unconfiguredSchemaMetaDataMap;
        allSchemaMetaData = createSchemaMetaData();
    }
    
    private SchemaMetaData createSchemaMetaData() {
        SchemaMetaData result = new SchemaMetaData();
        unconfiguredSchemaMetaDataMap.values().stream().flatMap(Collection::stream).forEach(tableName -> result.put(tableName, new TableMetaData()));
        result.merge(configuredSchemaMetaData);
        return result;
    }
    
    /**
     * Get schema meta data.
     *
     * @return schema meta data
     */
    public SchemaMetaData getSchemaMetaData() {
        return allSchemaMetaData;
    }
    
    /**
     * Get all table names.
     *
     * @return all table names
     */
    public Collection<String> getAllTableNames() {
        Collection<String> result = new LinkedList<>(configuredSchemaMetaData.getAllTableNames());
        unconfiguredSchemaMetaDataMap.values().forEach(result::addAll);
        return result;
    }
}
