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

package org.apache.shardingsphere.infra.optimize.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Federation schema meta data.
 */
@Getter
public final class FederationSchemaMetaData {
    
    private final String name;
    
    private final Map<String, FederationTableMetaData> tables;
    
    public FederationSchemaMetaData(final String name, final Map<String, TableMetaData> metaData) {
        this.name = name;
        this.tables = new ConcurrentHashMap<>(metaData.size(), 1);
        for (Entry<String, TableMetaData> entry : metaData.entrySet()) {
            tables.put(entry.getKey().toLowerCase(), new FederationTableMetaData(entry.getValue().getName(), entry.getValue()));
        }
    }
    
    /**
     * Add table meta data.
     * 
     * @param metaData table meta data to be updated
     */
    public void put(final TableMetaData metaData) {
        tables.put(metaData.getName().toLowerCase(), new FederationTableMetaData(metaData.getName(), metaData));
    }
    
    /**
     * Remove table meta data.
     * 
     * @param tableName table name to be removed
     */
    public void remove(final String tableName) {
        tables.remove(tableName.toLowerCase());
    }
}
