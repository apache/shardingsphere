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

package org.apache.shardingsphere.infra.metadata.data;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Sharding sphere data factory.
 */
public final class ShardingSphereDataFactory {
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
    /**
     * Init.
     *
     * @param metaData meta data
     * @return sharding sphere data
     */
    public static ShardingSphereData init(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getDatabases().values().iterator().next().getProtocolType();
        if (protocolType instanceof MySQLDatabaseType) {
            return initForMySQL(metaData);
        }
        if (protocolType instanceof PostgreSQLDatabaseType || protocolType instanceof OpenGaussDatabaseType) {
            return initForPostgreSQL(metaData);
        }
        return new ShardingSphereData();
    }
    
    private static ShardingSphereData initForMySQL(final ShardingSphereMetaData metaData) {
        ShardingSphereData result = new ShardingSphereData();
        Optional<ShardingSphereSchema> shardingSphereSchema = Optional.ofNullable(metaData.getDatabase(SHARDING_SPHERE)).map(database -> database.getSchema(SHARDING_SPHERE));
        if (!shardingSphereSchema.isPresent()) {
            return result;
        }
        ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
        for (Entry<String, ShardingSphereTable> entry : shardingSphereSchema.get().getTables().entrySet()) {
            schemaData.getTableData().put(entry.getKey(), initShardingSphereTableData(entry.getValue()));
        }
        ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
        databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
        result.getDatabaseData().put(SHARDING_SPHERE, databaseData);
        return result;
    }
    
    private static ShardingSphereData initForPostgreSQL(final ShardingSphereMetaData metaData) {
        ShardingSphereData result = new ShardingSphereData();
        for (Entry<String, ShardingSphereDatabase> entry : metaData.getDatabases().entrySet()) {
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            Optional<ShardingSphereSchema> shardingSphereSchema = Optional.ofNullable(entry.getValue()).map(database -> database.getSchema(SHARDING_SPHERE));
            if (shardingSphereSchema.isPresent()) {
                ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
                shardingSphereSchema.get().getTables().forEach((key, value) -> schemaData.getTableData().put(key, initShardingSphereTableData(value)));
                databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
            }
            result.getDatabaseData().put(entry.getKey(), databaseData);
        }
        return result;
    }
    
    private static ShardingSphereTableData initShardingSphereTableData(final ShardingSphereTable shardingSphereTable) {
        return new ShardingSphereTableData(shardingSphereTable.getName());
    }
}
