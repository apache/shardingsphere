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
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPositionFactory;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

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
    private final InventoryDumperConfiguration dumperConfig;
    
    private final PipelineChannel channel;
    
    private final PipelineSQLBuilder pipelineSQLBuilder;
    
    private final ColumnValueReader columnValueReader;
    
    private final DataSource dataSource;
    
    private final int batchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final LazyInitializer<PipelineTableMetaData> tableMetaDataLazyInitializer;
    
    protected AbstractInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final PipelineChannel channel,
                                      final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        if (!StandardPipelineDataSourceConfiguration.class.equals(inventoryDumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedSQLOperationException("AbstractInventoryDumper only support StandardPipelineDataSourceConfiguration");
        }
        this.dumperConfig = inventoryDumperConfig;
        this.channel = channel;
        pipelineSQLBuilder = PipelineSQLBuilderFactory.getInstance(inventoryDumperConfig.getDataSourceConfig().getDatabaseType().getType());
        columnValueReader = ColumnValueReaderFactory.getInstance(inventoryDumperConfig.getDataSourceConfig().getDatabaseType().getType());
        this.dataSource = dataSource;
        batchSize = inventoryDumperConfig.getBatchSize();
        rateLimitAlgorithm = inventoryDumperConfig.getRateLimitAlgorithm();
        tableMetaDataLazyInitializer = new LazyInitializer<PipelineTableMetaData>() {
            
            @Override
            protected PipelineTableMetaData initialize() {
                String schemaName = inventoryDumperConfig.getSchemaName(new LogicTableName(inventoryDumperConfig.getLogicTableName()));
                return metaDataLoader.getTableMetaData(schemaName, inventoryDumperConfig.getActualTableName());
            }
        };
    }
    
    @Override
    protected void doStart() {
        dump();
    }
    
    private void dump() {
        String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
        int uniqueKeyDataType = dumperConfig.getUniqueKeyDataType();
        String firstSQL = pipelineSQLBuilder.buildInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), uniqueKeyDataType, true);
        String laterSQL = pipelineSQLBuilder.buildInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), uniqueKeyDataType, false);
        IngestPosition<?> position = dumperConfig.getPosition();
        log.info("inventory dump, uniqueKeyDataType={}, firstSQL={}, laterSQL={}, position={}", uniqueKeyDataType, firstSQL, laterSQL, position);
        if (position instanceof FinishedPosition) {
            log.info("It is already finished, ignore");
            return;
        }
        Object startUniqueKeyValue = getPositionBeginValue(position);
        try (Connection conn = dataSource.getConnection()) {
            int round = 1;
            Optional<Object> maxUniqueKeyValue;
            while ((maxUniqueKeyValue = dump0(conn, 1 == round ? firstSQL : laterSQL, dumperConfig.getUniqueKey(), uniqueKeyDataType, startUniqueKeyValue, round++)).isPresent()) {
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
    
    @SneakyThrows(ConcurrentException.class)
    private PipelineTableMetaData getTableMetaData() {
        return tableMetaDataLazyInitializer.get();
    }
    
    private Optional<Object> dump0(final Connection conn, final String sql, final String uniqueKey, final int uniqueKeyDataType, final Object startUniqueKeyValue,
                                   final int round) throws SQLException {
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
        }
        PipelineTableMetaData tableMetaData = getTableMetaData();
        try (PreparedStatement preparedStatement = createPreparedStatement(conn, sql)) {
            preparedStatement.setFetchSize(batchSize);
            if (PipelineJdbcUtils.isIntegerColumn(uniqueKeyDataType)) {
                preparedStatement.setObject(1, startUniqueKeyValue);
                preparedStatement.setObject(2, getPositionEndValue(dumperConfig.getPosition()));
                preparedStatement.setInt(3, batchSize);
            } else if (PipelineJdbcUtils.isStringColumn(uniqueKeyDataType)) {
                preparedStatement.setObject(1, startUniqueKeyValue);
                preparedStatement.setInt(2, batchSize);
            } else {
                throw new IllegalArgumentException("Unsupported uniqueKeyDataType: " + uniqueKeyDataType);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int rowCount = 0;
                Object maxUniqueKeyValue = null;
                String logicTableName = dumperConfig.getLogicTableName();
                while (resultSet.next()) {
                    DataRecord record = new DataRecord(newPosition(resultSet), resultSetMetaData.getColumnCount());
                    record.setType(IngestDataChangeType.INSERT);
                    record.setTableName(logicTableName);
                    maxUniqueKeyValue = columnValueReader.readValue(resultSet, resultSetMetaData, tableMetaData.getColumnMetaData(uniqueKey).getOrdinalPosition());
                    for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                        boolean isUniqueKey = tableMetaData.isUniqueKey(i - 1);
                        record.addColumn(new Column(resultSetMetaData.getColumnName(i), columnValueReader.readValue(resultSet, resultSetMetaData, i), true, isUniqueKey));
                    }
                    pushRecord(record);
                    rowCount++;
                    if (!isRunning()) {
                        log.info("dump, running is false, break");
                        break;
                    }
                }
                if (0 == round % 50) {
                    log.info("dump, round={}, rowCount={}, maxUniqueKeyValue={}", round, rowCount, maxUniqueKeyValue);
                }
                return Optional.ofNullable(maxUniqueKeyValue);
            }
        }
    }
    
    private Object getPositionBeginValue(final IngestPosition<?> position) {
        return ((PrimaryKeyPosition<?>) position).getBeginValue();
    }
    
    private Object getPositionEndValue(final IngestPosition<?> position) {
        return ((PrimaryKeyPosition<?>) position).getEndValue();
    }
    
    private IngestPosition<?> newPosition(final ResultSet rs) throws SQLException {
        return null == dumperConfig.getUniqueKey() ? new PlaceholderPosition()
                : PrimaryKeyPositionFactory.newInstance(rs.getObject(dumperConfig.getUniqueKey()), ((PrimaryKeyPosition<?>) dumperConfig.getPosition()).getEndValue());
    }
    
    protected abstract PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException;
    
    private void pushRecord(final Record record) {
        channel.pushRecord(record);
    }
    
    @Override
    protected void doStop() {
    }
}
