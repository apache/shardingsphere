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

package org.apache.shardingsphere.sqlfederation.optimizer.executor;

import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.query.ShardingSphereDataQueryEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.memory.MemorySchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * ShardingSphere data federation query engine.
 */
public final class ShardingSphereDataFederationQueryEngine implements ShardingSphereDataQueryEngine {
    
    private ShardingSphereMetaData metaData;
    
    private Map<String, ShardingSphereDatabaseData> databaseData;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final Map<String, ShardingSphereDatabaseData> databaseData) {
        this.databaseData = databaseData;
        this.metaData = metaData;
    }
    
    @Override
    public Connection createConnection(final String databaseName, final String schemaName) throws SQLException {
        Map<String, ShardingSphereTableData> tableDataMap = Optional.ofNullable(databaseData.get(databaseName)).map(database -> database.getSchemaData().get(schemaName))
                .map(ShardingSphereSchemaData::getTableData).orElseGet(LinkedHashMap::new);
        ShardingSphereSchema shardingSphereSchema = Optional.ofNullable(metaData.getDatabase(databaseName)).map(database -> database.getSchema(schemaName))
                .orElseGet(ShardingSphereSchema::new);
        MemorySchema memorySchema = new MemorySchema(tableDataMap, shardingSphereSchema);
        Properties info = new Properties();
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        Connection result = DriverManager.getConnection("jdbc:calcite:", info);
        CalciteConnection calciteConnection = result.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        rootSchema.add(schemaName, memorySchema);
        return result;
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
