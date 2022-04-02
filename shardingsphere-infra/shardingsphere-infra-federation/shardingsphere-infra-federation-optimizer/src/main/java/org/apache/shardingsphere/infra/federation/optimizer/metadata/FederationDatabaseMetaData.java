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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
     * Add table meta data.
     *
     * @param metaData table meta data to be updated
     */
    public void put(final TableMetaData metaData) {
        if (schemas.containsKey(name)) {
            schemas.get(name).put(metaData);
        } else {
            Map<String, TableMetaData> tableMetaData = new LinkedHashMap<>();
            tableMetaData.put(name, metaData);
            schemas.put(name, new FederationSchemaMetaData(name, tableMetaData));
        }
    }
    
    /**
     * Remove table meta data.
     *
     * @param tableName table name to be removed
     */
    public void remove(final String tableName) {
        if (schemas.containsKey(name)) {
            schemas.get(name).remove(tableName);
        }
    }
}
