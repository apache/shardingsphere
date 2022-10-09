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

package org.apache.shardingsphere.infra.metadata.data.builder.dialect;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;

import java.util.Map;
import java.util.Optional;

/**
 * Postgre SQL ShardingSphere data Builder.
 */

public final class PostgreSQLShardingSphereDataBuilder implements ShardingSphereDataBuilder {
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
    @Override
    public ShardingSphereData build(final ShardingSphereMetaData metaData) {
        ShardingSphereData result = new ShardingSphereData();
        for (Map.Entry<String, ShardingSphereDatabase> entry : metaData.getDatabases().entrySet()) {
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            Optional<ShardingSphereSchema> shardingSphereSchema = Optional.ofNullable(entry.getValue()).map(database -> database.getSchema(SHARDING_SPHERE));
            if (shardingSphereSchema.isPresent()) {
                ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
                shardingSphereSchema.get().getTables().forEach((key, value) -> schemaData.getTableData().put(key, new ShardingSphereTableData(entry.getValue().getName())));
                databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
            }
            result.getDatabaseData().put(entry.getKey(), databaseData);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
