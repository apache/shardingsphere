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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

import javax.sql.DataSource;
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
    
    private final PipelineChannel channel;
    
    private final DataSource dataSource;
    
    private final int batchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final LazyInitializer<PipelineTableMetaData> tableMetaDataLazyInitializer;
    
    protected AbstractInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final PipelineChannel channel,
                                      final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        if (!StandardPipelineDataSourceConfiguration.class.equals(inventoryDumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedOperationException("AbstractInventoryDumper only support StandardPipelineDataSourceConfiguration");
        }
        this.inventoryDumperConfig = inventoryDumperConfig;
        this.channel = channel;
        this.dataSource = dataSource;
        batchSize = inventoryDumperConfig.getBatchSize();
        rateLimitAlgorithm = inventoryDumperConfig.getRateLimitAlgorithm();
        tableMetaDataLazyInitializer = new LazyInitializer<PipelineTableMetaData>() {
            
            @Override
            protected PipelineTableMetaData initialize() {
                return metaDataLoader.getTableMetaData(inventoryDumperConfig.getTableName());
            }
        };
    }
    
    @Override
    protected void doStart() {
        dump();
    }
    
    private void dump() {
        String sql = getDumpSQL();
        IngestPosition<?> position = inventoryDumperConfig.getPosition();
        log.info("inventory dump, sql={}, position={}", sql, position);
        try (Connection conn = dataSource.getConnection()) {
            int round = 1;
            Number startUniqueKeyValue = getPositionBeginValue(position) - 1;
            Optional<Number> maxUniqueKeyValue;
            while ((maxUniqueKeyValue = dump0(conn, sql, startUniqueKeyValue, round++)).isPresent()) {
                startUniqueKeyValue = maxUniqueKeyValue.get();
                if (!isRunning()) {
                    log.info("inventory dump, running is false, break");
                    break;
                }
            }
            log.info("inventory dump done, round={}, maxUniqueKeyValue={}", round, maxUniqueKeyValue);
        } catch (final SQLException ex) {
            log.error("inventory dump, ex caught, msg={}", ex.getMessage());
            throw new IngestException(ex);
        } finally {
            log.info("inventory dump, before put FinishedRecord");
            pushRecord(new FinishedRecord(new FinishedPosition()));
        }
    }
    
    private String getDumpSQL() {
        String tableName = inventoryDumperConfig.getTableName();
        String primaryKey = inventoryDumperConfig.getPrimaryKey();
        return "SELECT * FROM " + tableName + " WHERE " + primaryKey + " > ? AND " + primaryKey + " <= ? ORDER BY " + primaryKey + " ASC LIMIT ?";
    }
    
    @SneakyThrows(ConcurrentException.class)
    private PipelineTableMetaData getTableMetaData() {
        return tableMetaDataLazyInitializer.get();
    }
    
    private Optional<Number> dump0(final Connection conn, final String sql, final Number startUniqueKeyValue, final int round) throws SQLException {
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
        }
        PipelineTableMetaData tableMetaData = getTableMetaData();
        try (PreparedStatement preparedStatement = createPreparedStatement(conn, sql)) {
            preparedStatement.setObject(1, startUniqueKeyValue);
            preparedStatement.setObject(2, getPositionEndValue(inventoryDumperConfig.getPosition()));
            preparedStatement.setInt(3, batchSize);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int rowCount = 0;
                Number maxUniqueKeyValue = null;
                while (resultSet.next()) {
                    DataRecord record = new DataRecord(newPosition(resultSet), metaData.getColumnCount());
                    record.setType(IngestDataChangeType.INSERT);
                    record.setTableName(inventoryDumperConfig.getTableNameMap().get(inventoryDumperConfig.getTableName()));
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        boolean isPrimaryKey = tableMetaData.isPrimaryKey(i - 1);
                        Object value = readValue(resultSet, i);
                        if (isPrimaryKey) {
                            maxUniqueKeyValue = (Number) value;
                        }
                        record.addColumn(new Column(metaData.getColumnName(i), value, true, isPrimaryKey));
                    }
                    pushRecord(record);
                    rowCount++;
                    if (!isRunning()) {
                        log.info("dump, running is false, break");
                        break;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("dump, round={}, rowCount={}, maxUniqueKeyValue={}", round, rowCount, maxUniqueKeyValue);
                } else if (0 == round % 50) {
                    log.info("dump, round={}, rowCount={}, maxUniqueKeyValue={}", round, rowCount, maxUniqueKeyValue);
                }
                return Optional.ofNullable(maxUniqueKeyValue);
            }
        }
    }
    
    private long getPositionBeginValue(final IngestPosition<?> position) {
        return position instanceof PrimaryKeyPosition ? ((PrimaryKeyPosition) position).getBeginValue() : 0;
    }
    
    private long getPositionEndValue(final IngestPosition<?> position) {
        return position instanceof PrimaryKeyPosition ? ((PrimaryKeyPosition) position).getEndValue() : Integer.MAX_VALUE;
    }
    
    private IngestPosition<?> newPosition(final ResultSet rs) throws SQLException {
        return null == inventoryDumperConfig.getPrimaryKey() ? new PlaceholderPosition()
                : new PrimaryKeyPosition(rs.getLong(inventoryDumperConfig.getPrimaryKey()), ((PrimaryKeyPosition) inventoryDumperConfig.getPosition()).getEndValue());
    }
    
    protected abstract PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException;
    
    protected Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        return resultSet.getObject(index);
    }
    
    private void pushRecord(final Record record) {
        channel.pushRecord(record);
    }
    
    @Override
    protected void doStop() {
    }
}
