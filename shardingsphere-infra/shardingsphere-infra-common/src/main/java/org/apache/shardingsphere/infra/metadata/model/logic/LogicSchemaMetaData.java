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

package org.apache.shardingsphere.infra.metadata.model.logic;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;

import java.util.Collection;
import java.util.Map;

/**
 * Logic schema meta data.
 */
public final class LogicSchemaMetaData {
    
    @Getter
    private final PhysicalSchemaMetaData configuredSchemaMetaData;
    
    @Getter
    private final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap;
    
    private final PhysicalSchemaMetaData allSchemaMetaData;
    
    public LogicSchemaMetaData(final PhysicalSchemaMetaData configuredSchemaMetaData, final Map<String, Collection<String>> unconfiguredSchemaMetaDataMap) {
        this.configuredSchemaMetaData = configuredSchemaMetaData;
        this.unconfiguredSchemaMetaDataMap = unconfiguredSchemaMetaDataMap;
        allSchemaMetaData = createSchemaMetaData();
    }
    
    private PhysicalSchemaMetaData createSchemaMetaData() {
        PhysicalSchemaMetaData result = new PhysicalSchemaMetaData();
        unconfiguredSchemaMetaDataMap.values().stream().flatMap(Collection::stream).forEach(tableName -> result.put(tableName, new PhysicalTableMetaData()));
        result.merge(configuredSchemaMetaData);
        return result;
    }
    
    /**
     * Get schema meta data.
     *
     * @return schema meta data
     */
    public PhysicalSchemaMetaData getSchemaMetaData() {
        return allSchemaMetaData;
    }
}
