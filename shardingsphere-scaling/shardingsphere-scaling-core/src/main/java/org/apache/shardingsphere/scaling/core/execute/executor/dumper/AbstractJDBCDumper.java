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

package org.apache.shardingsphere.scaling.core.execute.executor.dumper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.scaling.core.utils.RdbmsConfigurationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Abstract JDBC dumper implement.
 */
@Slf4j
public abstract class AbstractJDBCDumper extends AbstractScalingExecutor implements JDBCDumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final InventoryDumperConfiguration inventoryDumperConfig;
    
    private final DataSourceManager dataSourceManager;
    
    private final TableMetaData tableMetaData;
    
    @Setter
    private Channel channel;
    
    protected AbstractJDBCDumper(final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        if (!StandardJDBCDataSourceConfiguration.class.equals(inventoryDumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedOperationException("AbstractJDBCDumper only support JDBCDataSourceConfiguration");
        }
        this.inventoryDumperConfig = inventoryDumperConfig;
        this.dataSourceManager = dataSourceManager;
        tableMetaData = createTableMetaData();
    }
    
    private TableMetaData createTableMetaData() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(inventoryDumperConfig.getDataSourceConfig()));
        return metaDataManager.getTableMetaData(inventoryDumperConfig.getTableName());
    }
    
    @Override
    public final void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        try (Connection conn = dataSourceManager.getDataSource(inventoryDumperConfig.getDataSourceConfig()).getConnection()) {
            String sql = String.format("SELECT * FROM %s %s", inventoryDumperConfig.getTableName(), RdbmsConfigurationUtil.getWhereCondition(inventoryDumperConfig));
            PreparedStatement ps = createPreparedStatement(conn, sql);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (isRunning() && rs.next()) {
                DataRecord record = new DataRecord(newPosition(rs), metaData.getColumnCount());
                record.setType(ScalingConstant.INSERT);
                record.setTableName(inventoryDumperConfig.getTableNameMap().get(inventoryDumperConfig.getTableName()));
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.addColumn(new Column(metaData.getColumnName(i), readValue(rs, i), true, tableMetaData.isPrimaryKey(i - 1)));
                }
                pushRecord(record);
            }
            pushRecord(new FinishedRecord(new FinishedPosition()));
        } catch (final SQLException ex) {
            stop();
            channel.close();
            throw new ScalingTaskExecuteException(ex);
        } finally {
            pushRecord(new FinishedRecord(new PlaceholderPosition()));
        }
    }
    
    private Position<?> newPosition(final ResultSet rs) throws SQLException {
        if (null == inventoryDumperConfig.getPrimaryKey()) {
            return new PlaceholderPosition();
        }
        return new PrimaryKeyPosition(rs.getLong(inventoryDumperConfig.getPrimaryKey()), ((PrimaryKeyPosition) inventoryDumperConfig.getPositionManager().getPosition()).getEndValue());
    }
    
    protected abstract PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException;
    
    protected Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        return resultSet.getObject(index);
    }
    
    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (final InterruptedException ignored) {
        }
    }
}
