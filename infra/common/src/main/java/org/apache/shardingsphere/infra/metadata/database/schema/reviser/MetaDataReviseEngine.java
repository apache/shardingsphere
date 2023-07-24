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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema.SchemaMetaDataReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Meta data revise engine.
 */
@RequiredArgsConstructor
public final class MetaDataReviseEngine {
    
    private final Collection<ShardingSphereRule> rules;
    
    /**
     * Revise meta data.
     * 
     * @param schemaMetaDataMap schema meta data map
     * @param material generic schema builder material
     * @return revised meta data
     */
    public Map<String, SchemaMetaData> revise(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap.size(), 1F);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            // TODO establish a corresponding relationship between tables and data sources
            DatabaseType databaseType = material.getStorageTypes().values().stream().findFirst().orElse(null);
            DataSource dataSource = material.getDataSourceMap().values().stream().findFirst().orElse(null);
            result.put(entry.getKey(), new SchemaMetaDataReviseEngine(rules, material.getProps(), databaseType, dataSource).revise(entry.getValue()));
        }
        return result;
    }
}
