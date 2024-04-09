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

package org.apache.shardingsphere.infra.metadata.statistics;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere database data.
 */
@Getter
public final class ShardingSphereDatabaseData {
    
    private final Map<String, ShardingSphereSchemaData> schemaData = new LinkedHashMap<>();
    
    /**
     * Get ShardingSphere schema data.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema data
     */
    public ShardingSphereSchemaData getSchema(final String schemaName) {
        return schemaData.get(schemaName.toLowerCase());
    }
    
    /**
     * Put ShardingSphere schema data.
     *
     * @param schemaName schema name
     * @param schema ShardingSphere schema data
     */
    public void putSchema(final String schemaName, final ShardingSphereSchemaData schema) {
        schemaData.put(schemaName.toLowerCase(), schema);
    }
    
    /**
     * Remove ShardingSphere schema data.
     *
     * @param schemaName schema name
     */
    public void removeSchema(final String schemaName) {
        schemaData.remove(schemaName.toLowerCase());
    }
    
    /**
     * Judge contains ShardingSphere schema from ShardingSphere database or not.
     *
     * @param schemaName schema name
     * @return Contains schema from database or not
     */
    public boolean containsSchema(final String schemaName) {
        return schemaData.containsKey(schemaName.toLowerCase());
    }
}
