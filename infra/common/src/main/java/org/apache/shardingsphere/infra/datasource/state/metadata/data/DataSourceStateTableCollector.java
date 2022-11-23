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

package org.apache.shardingsphere.infra.datasource.state.metadata.data;

import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data source state table data collector.
 */
public final class DataSourceStateTableCollector implements ShardingSphereDataCollector {
    
    private static final String DATA_SOURCE_STATE_TABLE = "data_source_state";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String currentDatabaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(DATA_SOURCE_STATE_TABLE, new ArrayList<>(table.getColumns().values()));
        shardingSphereDatabases.forEach((key, value) -> {
            collectPhysicalDataSourceStates(result, key, value);
            collectLogicalDataSourceStates(result, key, value);
        });
        return Optional.of(result);
    }
    
    private void collectPhysicalDataSourceStates(final ShardingSphereTableData tableData, final String databaseName, final ShardingSphereDatabase database) {
        database.getResourceMetaData().getDataSources().forEach((key, value) -> {
            if (DataSourceState.DISABLED.equals(DataSourceStateManager.getInstance().getLogicalState(databaseName, key))) {
                List<Object> row = createRowData(databaseName, key, DataSourceState.DISABLED.toString(), "");
                tableData.getRows().add(new ShardingSphereRowData(row));
            } else {
                DataSourceState newState = checkPhysicalState(value);
                DataSourceStateManager.getInstance().updatePhysicalState(databaseName, key, newState);
                List<Object> row = createRowData(databaseName, key, DataSourceState.ENABLED.toString(), String.valueOf(newState));
                tableData.getRows().add(new ShardingSphereRowData(row));
            }
        });
    }
    
    private List<Object> createRowData(final String databaseName, final String dataSourceName, final String logicalState, final String physicalState) {
        List<Object> row = new LinkedList<>();
        row.add(databaseName);
        row.add(dataSourceName);
        row.add(logicalState);
        row.add(physicalState);
        return row;
    }
    
    private DataSourceState checkPhysicalState(final DataSource dataSource) {
        try (Connection ignored = dataSource.getConnection()) {
            return DataSourceState.OK;
        } catch (final SQLException ignored) {
            return DataSourceState.ERROR;
        }
    }
    
    private void collectLogicalDataSourceStates(final ShardingSphereTableData tableData, final String databaseName, final ShardingSphereDatabase database) {
        database.getRuleMetaData().getRules().forEach(rule -> {
            if (rule instanceof DataSourceContainedRule) {
                Map<String, DataSourceState> logicDataSourceStates = ((DataSourceContainedRule) rule).calculateLogicalDataSourceStates();
                logicDataSourceStates.forEach((key, value) -> {
                    DataSourceStateManager.getInstance().updateLogicalState(databaseName, key, value);
                    List<Object> row = createRowData(databaseName, key, String.valueOf(value), "");
                    tableData.getRows().add(new ShardingSphereRowData(row));
                });
            }
        });
    }
    
    @Override
    public String getType() {
        return DATA_SOURCE_STATE_TABLE;
    }
}
