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

package org.apache.shardingsphere.sharding.metadata.data;

import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage unit state table data collector.
 */
public final class StorageUnitStateTableCollector implements ShardingSphereDataCollector {
    
    private static final String STORAGE_UNIT_STATE_TABLE = "storage_unit_state";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String currentDatabaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(STORAGE_UNIT_STATE_TABLE, new ArrayList<>(table.getColumns().values()));
        shardingSphereDatabases.forEach((key, value) -> {
            if (!value.getResourceMetaData().getDataSources().isEmpty()) {
                value.getResourceMetaData().getDataSources().forEach((dataSourceName, dataSource) -> {
                    DataSourceState state = DataSourceStateManager.getInstance().getState(key, dataSourceName);
                    if (!DataSourceState.DISABLED.equals(state)) {
                        List<Object> row = new LinkedList<>();
                        row.add(key);
                        row.add(dataSourceName);
                        row.add(String.valueOf(checkState(dataSource)));
                        result.getRows().add(new ShardingSphereRowData(row));
                    }
                });
            }
        });
        return Optional.of(result);
    }
    
    private DataSourceState checkState(final DataSource dataSource) {
        try (Connection ignored = dataSource.getConnection()) {
            return DataSourceState.ENABLED;
        } catch (final SQLException ex) {
            return DataSourceState.ERROR;
        }
    }
    
    @Override
    public String getType() {
        return STORAGE_UNIT_STATE_TABLE;
    }
}
