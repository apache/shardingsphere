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

package org.apache.shardingsphere.infra.metadata.statistics.collector.opengauss.pgcatalog;

import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.pgcatalog.PostgreSQLPgClassTableCollector;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Table pg_catalog.pg_class data collector for openGauss.
 */
public final class OpenGaussPgClassTableCollector implements ShardingSphereStatisticsCollector {
    
    private final PostgreSQLPgClassTableCollector delegated = new PostgreSQLPgClassTableCollector();
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        return delegated.collect(databaseName, table, shardingSphereDatabases);
    }
    
    @Override
    public String getType() {
        return String.join(".", new OpenGaussDatabaseType().getType(), "pg_catalog", "pg_class");
    }
}
