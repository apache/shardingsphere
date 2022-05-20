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

package org.apache.shardingsphere.infra.metadata.database;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere database.
 */
@Getter
public final class ShardingSphereDatabase {
    
    private final String name;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    public ShardingSphereDatabase(final String name, final Map<String, ShardingSphereSchema> schemas) {
        this.name = name;
        this.schemas = new ConcurrentHashMap<>(schemas.size(), 1);
        schemas.forEach((key, value) -> this.schemas.put(key.toLowerCase(), value));
    }
    
    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return schemas.get(schemaName);
    }
}
