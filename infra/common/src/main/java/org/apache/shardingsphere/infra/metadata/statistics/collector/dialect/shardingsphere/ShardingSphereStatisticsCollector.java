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

package org.apache.shardingsphere.infra.metadata.statistics.collector.dialect.shardingsphere;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.StatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;
import java.util.Optional;

/**
 * ShardingSphere statistics collector.
 */
public final class ShardingSphereStatisticsCollector implements StatisticsCollector {
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final String schemaName,
                                                     final ShardingSphereTable table, final ShardingSphereMetaData metaData) throws SQLException {
        Optional<ShardingSphereTableStatisticsCollector> statisticsCollector = TypedSPILoader
                .findService(ShardingSphereTableStatisticsCollector.class, String.format("%s.%s", schemaName, table.getName()));
        return statisticsCollector.isPresent() ? statisticsCollector.get().collect(databaseName, schemaName, table, metaData) : Optional.empty();
    }
    
    @Override
    public String getType() {
        return "shardingsphere";
    }
}
