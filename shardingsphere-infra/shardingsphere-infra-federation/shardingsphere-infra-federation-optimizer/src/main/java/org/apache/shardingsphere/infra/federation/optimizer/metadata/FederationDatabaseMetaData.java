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

package org.apache.shardingsphere.infra.federation.optimizer.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Federation database meta data.
 */
@Getter
public final class FederationDatabaseMetaData {
    
    private final String name;
    
    private final Map<String, FederationSchemaMetaData> schemas;
    
    public FederationDatabaseMetaData(final String name, final Map<String, ShardingSphereSchema> schemas) {
        this.name = name;
        this.schemas = new ConcurrentHashMap<>(schemas.size(), 1);
        for (Entry<String, ShardingSphereSchema> entry : schemas.entrySet()) {
            this.schemas.put(entry.getKey().toLowerCase(), new FederationSchemaMetaData(entry.getKey(), entry.getValue().getTables()));
        }
    }
    
    /**
     * Put schema meta data.
     *
     * @param schemaName schema name
     * @param schemaMetaData schema metadata
     */
    public void putSchemaMetadata(final String schemaName, final FederationSchemaMetaData schemaMetaData) {
        schemas.put(schemaName.toLowerCase(), schemaMetaData);
    }
    
    /**
     * Put table.
     *
     * @param schemaName schema name
     * @param table table
     */
    public void putTable(final String schemaName, final ShardingSphereTable table) {
        FederationSchemaMetaData schemaMetaData = schemas.computeIfAbsent(schemaName.toLowerCase(), key -> new FederationSchemaMetaData(schemaName, new LinkedHashMap<>()));
        schemaMetaData.put(table);
    }
    
    /**
     * Remove schema meta data.
     *
     * @param schemaName schema name
     */
    public void removeSchemaMetadata(final String schemaName) {
        schemas.remove(schemaName.toLowerCase());
    }
    
    /**
     * Remove table meta data.
     *
     * @param schemaName schema name
     * @param tableName table name
     */
    public void removeTableMetadata(final String schemaName, final String tableName) {
        if (schemas.containsKey(schemaName.toLowerCase())) {
            schemas.get(schemaName.toLowerCase()).remove(tableName.toLowerCase());
        }
    }
    
    /**
     * Get table meta data.
     *
     * @param schemaName schema name
     *
     * @return FederationSchemaMetaData schema meta data
     */
    public Optional<FederationSchemaMetaData> getSchemaMetadata(final String schemaName) {
        return Optional.ofNullable(schemas.get(schemaName));
    }
}
