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
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.scaling.core.utils.RdbmsConfigurationUtil;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Abstract JDBC dumper implement.
 */
@Slf4j
public abstract class AbstractJDBCDumper extends AbstractShardingScalingExecutor<InventoryPosition> implements JDBCDumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final DumperConfiguration rdbmsConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final TableMetaData tableMetaData;
    
    @Setter
    private Channel channel;
    
    protected AbstractJDBCDumper(final DumperConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        if (!JDBCDataSourceConfiguration.class.equals(rdbmsConfiguration.getDataSourceConfiguration().getClass())) {
            throw new UnsupportedOperationException("AbstractJDBCDumper only support JDBCDataSourceConfiguration");
        }
        this.rdbmsConfiguration = rdbmsConfiguration;
        this.dataSourceManager = dataSourceManager;
        tableMetaData = createTableMetaData();
    }
    
    private TableMetaData createTableMetaData() {
        MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(rdbmsConfiguration.getDataSourceConfiguration()));
        return metaDataManager.getTableMetaData(rdbmsConfiguration.getTableName());
    }
    
    @Override
    public final void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        try (Connection conn = dataSourceManager.getDataSource(rdbmsConfiguration.getDataSourceConfiguration()).getConnection()) {
            String sql = String.format("SELECT * FROM %s %s", rdbmsConfiguration.getTableName(), RdbmsConfigurationUtil.getWhereCondition(rdbmsConfiguration));
            PreparedStatement ps = createPreparedStatement(conn, sql);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (isRunning() && rs.next()) {
                DataRecord record = new DataRecord(newInventoryPosition(rs), metaData.getColumnCount());
                record.setType(ScalingConstant.INSERT);
                record.setTableName(rdbmsConfiguration.getTableNameMap().get(rdbmsConfiguration.getTableName()));
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.addColumn(new Column(metaData.getColumnName(i), readValue(rs, i), true, tableMetaData.isPrimaryKey(i)));
                }
                pushRecord(record);
            }
            pushRecord(new FinishedRecord(new FinishedPosition()));
        } catch (final SQLException ex) {
            stop();
            channel.close();
            throw new SyncTaskExecuteException(ex);
        } finally {
            pushRecord(new FinishedRecord(new NopPosition()));
        }
    }
    
    private InventoryPosition newInventoryPosition(final ResultSet rs) throws SQLException {
        if (null == rdbmsConfiguration.getPrimaryKey()) {
            return new PlaceholderPosition();
        }
        return new PrimaryKeyPosition(rs.getLong(rdbmsConfiguration.getPrimaryKey()), ((PrimaryKeyPosition) rdbmsConfiguration.getPositionManager().getPosition()).getEndValue());
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
