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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.writer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.AbstractSyncRunner;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

/**
 * generic jdbc writer implement.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public abstract class AbstractJdbcWriter extends AbstractSyncRunner implements Writer {
    
    private final RdbmsConfiguration rdbmsConfiguration;
    
    private final DataSourceFactory dataSourceFactory;
    
    private final AbstractSqlBuilder sqlBuilder;
    
    private MetaDataManager metaDataManager;
    
    @Setter
    private Channel channel;
    
    public AbstractJdbcWriter(final RdbmsConfiguration rdbmsConfiguration, final DataSourceFactory dataSourceFactory) {
        this.rdbmsConfiguration = rdbmsConfiguration;
        this.dataSourceFactory = dataSourceFactory;
        DataSource dataSource = dataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration());
        metaDataManager = new MetaDataManager(dataSource);
        sqlBuilder = createSqlBuilder(metaDataManager);
    }
    
    /**
     * Create sql builder.
     *
     * @param metaDataManager database metadata util
     * @return sql builder
     */
    protected abstract AbstractSqlBuilder createSqlBuilder(MetaDataManager metaDataManager);
    
    @Override
    public final void run() {
        start();
        write();
    }
    
    @Override
    public final void write() {
        try {
            while (isRunning()) {
                List<Record> records = channel.fetchRecords(100, 3);
                if (null != records && 0 < records.size()) {
                    flush(dataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration()), records);
                    if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                        channel.ack();
                        break;
                    }
                }
                channel.ack();
            }
        } catch (SQLException ex) {
            throw new SyncTaskExecuteException(ex);
        }
    }
    
    private void flush(final DataSource dataSource, final List<Record> buffer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            for (Record record : buffer) {
                if (DataRecord.class.equals(record.getClass())) {
                    DataRecord dataRecord = (DataRecord) record;
                    if ("bootstrap-insert".equals(dataRecord.getType())
                            || "insert".equals(dataRecord.getType())) {
                        executeInsert(connection, dataRecord);
                    } else if ("update".equals(dataRecord.getType())) {
                        executeUpdate(connection, dataRecord);
                    } else if ("delete".equals(dataRecord.getType())) {
                        executeDelete(connection, dataRecord);
                    }
                }
            }
            connection.commit();
        }
    }
    
    private void executeInsert(final Connection connection, final DataRecord record) throws SQLException {
        String insertSql = sqlBuilder.buildInsertSql(record.getTableName());
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setQueryTimeout(30);
        try {
            for (int i = 0; i < record.getColumnCount(); i++) {
                ps.setObject(i + 1, record.getColumn(i).getValue());
            }
            ps.execute();
        } catch (SQLIntegrityConstraintViolationException ex) {
            // ignore
        }
    }
    
    private void executeUpdate(final Connection connection, final DataRecord record) throws SQLException {
        List<ColumnMetaData> metaData = metaDataManager.getColumnNames(record.getTableName());
        List<String> primaryKeys = metaDataManager.getPrimaryKeys(record.getTableName());
        List<ColumnMetaData> updatedColumns = new LinkedList<>();
        List<Column> values = new LinkedList<>();
        for (int i = 0; i < metaData.size(); i++) {
            if (record.getColumn(i).isUpdated()) {
                updatedColumns.add(metaData.get(i));
                values.add(record.getColumn(i));
            }
        }
        for (String primaryKey : primaryKeys) {
            int index = metaDataManager.findColumnIndex(metaData, primaryKey);
            values.add(record.getColumn(index));
        }
        String updateSql = sqlBuilder.buildUpdateSql(record.getTableName(), updatedColumns);
        PreparedStatement ps = connection.prepareStatement(updateSql);
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i).getValue());
        }
        ps.execute();
    }
    
    private void executeDelete(final Connection connection, final DataRecord record) throws SQLException {
        List<ColumnMetaData> metaData = metaDataManager.getColumnNames(record.getTableName());
        List<String> primaryKeys = metaDataManager.getPrimaryKeys(record.getTableName());
        String deleteSql = sqlBuilder.buildDeleteSql(record.getTableName());
        PreparedStatement ps = connection.prepareStatement(deleteSql);
        for (int i = 0; i < primaryKeys.size(); i++) {
            int index = metaDataManager.findColumnIndex(metaData, primaryKeys.get(i));
            ps.setObject(i + 1, record.getColumn(index).getValue());
        }
        ps.execute();
    }
}
