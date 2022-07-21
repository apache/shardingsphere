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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Federation meta data.
 */
@Getter
public final class FederationMetaData {
    
    private final Map<String, FederationDatabaseMetaData> databases;
    
    public FederationMetaData(final Map<String, ShardingSphereDatabase> databases) {
        this.databases = new ConcurrentHashMap<>(databases.size(), 1);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            this.databases.put(entry.getKey().toLowerCase(), new FederationDatabaseMetaData(entry.getKey(), entry.getValue().getSchemas()));
        }
    }
    
    /**
     * Get database.
     *
     * @param databaseName database name
     * @return database
     */
    public FederationDatabaseMetaData getDatabase(final String databaseName) {
        return databases.get(databaseName.toLowerCase());
    }
}
