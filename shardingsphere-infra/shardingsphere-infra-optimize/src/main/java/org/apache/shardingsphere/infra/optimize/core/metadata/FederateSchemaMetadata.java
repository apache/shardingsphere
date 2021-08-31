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

package org.apache.shardingsphere.infra.optimize.core.metadata;

import lombok.Getter;
import lombok.Synchronized;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.Map;
import java.util.TreeMap;


/**
 * Federate schema metadata.
 *
 */
@Getter
public final class FederateSchemaMetadata {
    
    private final String name;
    
    private final Map<String, FederateTableMetadata> tables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    public FederateSchemaMetadata(final String name, final Map<String, TableMetaData> metaData) {
        this.name = name;
        for (TableMetaData each : metaData.values()) {
            tables.put(each.getName(), new FederateTableMetadata(each.getName(), each));
        }
    }
    
    /**
     * Renew.
     * 
     * @param metaData meta data
     */
    @Synchronized
    public void renew(final TableMetaData metaData) {
        tables.put(metaData.getName(), new FederateTableMetadata(metaData.getName(), metaData));
    }
    
    /**
     * Remove.
     * 
     * @param tableName table name
     */
    @Synchronized
    public void remove(final String tableName) {
        tables.remove(tableName);
    }
}
