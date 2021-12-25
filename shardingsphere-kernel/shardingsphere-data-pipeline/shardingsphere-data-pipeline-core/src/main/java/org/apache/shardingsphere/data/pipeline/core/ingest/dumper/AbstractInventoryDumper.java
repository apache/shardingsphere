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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.MetaDataManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.impl.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Abstract JDBC dumper implement.
 */
@Slf4j
public abstract class AbstractInventoryDumper extends AbstractLifecycleExecutor implements InventoryDumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final InventoryDumperConfiguration inventoryDumperConfig;
    
    private final int readBatchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final DataSourceManager dataSourceManager;
    
    private final TableMetaData tableMetaData;
    
    @Setter
    private Channel channel;
    
    protected AbstractInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final DataSourceManager dataSourceManager) {
        if (!StandardJDBCDataSourceConfiguration.class.equals(inventoryDumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedOperationException("AbstractInventoryDumper only support StandardJDBCDataSourceConfiguration");
        }
        this.inventoryDumperConfig = inventoryDumperConfig;
        this.readBatchSize = inventoryDumperConfig.getReadBatchSize();
        this.rateLimitAlgorithm = inventoryDumperConfig.getRateLimitAlgorithm();
        this.dataSourceManager = dataSourceManager;
        tableMetaData = createTableMetaData();
    }
    
    private TableMetaData createTableMetaData() {
        JDBCDataSourceConfiguration dataSourceConfig = inventoryDumperConfig.getDataSourceConfig();
        // TODO share MetaDataManager
        MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(dataSourceConfig));
        return metaDataManager.getTableMetaData(inventoryDumperConfig.getTableName(), dataSourceConfig.getDatabaseType());
    }
    
    @Override
    public final void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        String sql = getDumpSQL();
        IngestPosition<?> position = inventoryDumperConfig.getPosition();
        log.info("inventory dump, sql={}, position={}", sql, position);
        try (Connection conn = dataSourceManager.getDataSource(inventoryDumperConfig.getDataSourceConfig()).getConnection()) {
            Number startPrimaryValue = getPositionBeginValue(position) - 1;
            Optional<Number> maxPrimaryValue;
            while ((maxPrimaryValue = dump0(conn, sql, startPrimaryValue)).isPresent()) {
                startPrimaryValue = maxPrimaryValue.get();
            }
        } catch (final SQLException ex) {
            stop();
            channel.close();
            throw new IngestException(ex);
        } finally {
            pushRecord(new FinishedRecord(new PlaceholderPosition()));
        }
    }
    
    private String getDumpSQL() {
        String tableName = inventoryDumperConfig.getTableName();
        String primaryKey = inventoryDumperConfig.getPrimaryKey();
        return "SELECT * FROM " + tableName + " WHERE " + primaryKey + " > ? AND " + primaryKey + " <= ? ORDER BY " + primaryKey + " ASC LIMIT ?";
    }
    
    private Optional<Number> dump0(final Connection conn, final String sql, final Number startPrimaryValue) throws SQLException {
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.onQuery();
        }
        try (PreparedStatement preparedStatement = createPreparedStatement(conn, sql)) {
            preparedStatement.setObject(1, startPrimaryValue);
            preparedStatement.setObject(2, getPositionEndValue(inventoryDumperConfig.getPosition()));
            preparedStatement.setInt(3, readBatchSize);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int rowCount = 0;
                Number maxPrimaryValue = null;
                while (isRunning() && resultSet.next()) {
                    DataRecord record = new DataRecord(newPosition(resultSet), metaData.getColumnCount());
                    record.setType(IngestDataChangeType.INSERT);
                    record.setTableName(inventoryDumperConfig.getTableNameMap().get(inventoryDumperConfig.getTableName()));
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        boolean isPrimaryKey = tableMetaData.isPrimaryKey(i - 1);
                        Object value = readValue(resultSet, i);
                        if (isPrimaryKey) {
                            maxPrimaryValue = (Number) value;
                        }
                        record.addColumn(new Column(metaData.getColumnName(i), value, true, isPrimaryKey));
                    }
                    pushRecord(record);
                    rowCount++;
                }
                log.info("dump, rowCount={}, maxPrimaryValue={}", rowCount, maxPrimaryValue);
                pushRecord(new FinishedRecord(new FinishedPosition()));
                return Optional.ofNullable(maxPrimaryValue);
            }
        }
    }
    
    private long getPositionBeginValue(final IngestPosition<?> position) {
        if (null == position) {
            return 0;
        }
        if (!(position instanceof PrimaryKeyPosition)) {
            return 0;
        }
        return ((PrimaryKeyPosition) position).getBeginValue();
    }
    
    private long getPositionEndValue(final IngestPosition<?> position) {
        if (null == position) {
            return Integer.MAX_VALUE;
        }
        if (!(position instanceof PrimaryKeyPosition)) {
            return Integer.MAX_VALUE;
        }
        return ((PrimaryKeyPosition) position).getEndValue();
    }
    
    private IngestPosition<?> newPosition(final ResultSet rs) throws SQLException {
        if (null == inventoryDumperConfig.getPrimaryKey()) {
            return new PlaceholderPosition();
        }
        return new PrimaryKeyPosition(rs.getLong(inventoryDumperConfig.getPrimaryKey()), ((PrimaryKeyPosition) inventoryDumperConfig.getPosition()).getEndValue());
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
