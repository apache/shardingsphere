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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.InventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.StreamingRangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.AbstractRecordTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.TableInventoryCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPositionFactory;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineInventoryDumpSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory dumper.
 */
@HighFrequencyInvocation
@Slf4j
public final class InventoryDumper extends AbstractPipelineLifecycleRunnable implements Dumper {
    
    private final InventoryDumperContext dumperContext;
    
    private final PipelineChannel channel;
    
    private final PipelineDataSource dataSource;
    
    private final InventoryDataRecordPositionCreator positionCreator;
    
    private final PipelineInventoryDumpSQLBuilder sqlBuilder;
    
    private final InventoryColumnValueReaderEngine columnValueReaderEngine;
    
    private final AtomicReference<Statement> runningStatement = new AtomicReference<>();
    
    public InventoryDumper(final InventoryDumperContext dumperContext, final PipelineChannel channel, final PipelineDataSource dataSource, final InventoryDataRecordPositionCreator positionCreator) {
        this.dumperContext = dumperContext;
        this.channel = channel;
        this.dataSource = dataSource;
        this.positionCreator = positionCreator;
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        sqlBuilder = new PipelineInventoryDumpSQLBuilder(databaseType);
        columnValueReaderEngine = new InventoryColumnValueReaderEngine(databaseType);
    }
    
    @Override
    protected void runBlocking() {
        IngestPosition position = dumperContext.getCommonContext().getPosition();
        if (position instanceof IngestFinishedPosition) {
            log.info("Ignored because of already finished.");
            return;
        }
        try {
            if (dumperContext.hasUniqueKey()) {
                dumpByCalculator();
            } else {
                dumpWithStreamingQuery();
            }
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Inventory dump failed on {}", dumperContext.getActualTableName(), ex);
            throw new IngestException("Inventory dump failed on " + dumperContext.getActualTableName(), ex);
        }
    }
    
    private void dumpByCalculator() {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        QualifiedTable table = new QualifiedTable(schemaName, dumperContext.getActualTableName());
        IngestPosition initialPosition = dumperContext.getCommonContext().getPosition();
        log.info("Dump by calculator start, dataSource={}, table={}, initialPosition={}", dumperContext.getCommonContext().getDataSourceName(), table, initialPosition);
        List<String> columnNames = dumperContext.getQueryColumnNames();
        TableInventoryCalculateParameter calculateParam = new TableInventoryCalculateParameter(dataSource, table,
                columnNames, dumperContext.getUniqueKeyColumns(), QueryType.RANGE_QUERY, null);
        Range range = new Range(((PrimaryKeyIngestPosition<?>) initialPosition).getBeginValue(), true, ((PrimaryKeyIngestPosition<?>) initialPosition).getEndValue());
        calculateParam.setRange(range);
        RecordTableInventoryDumpCalculator dumpCalculator = new RecordTableInventoryDumpCalculator(dumperContext.getBatchSize(), StreamingRangeType.SMALL);
        long rowCount = 0L;
        try {
            String firstUniqueKey = calculateParam.getFirstUniqueKey().getName();
            for (List<DataRecord> each : dumpCalculator.calculate(calculateParam)) {
                channel.push(Collections.unmodifiableList(each));
                IngestPosition position = PrimaryKeyIngestPositionFactory.newInstance(dumpCalculator.getFirstUniqueKeyValue(each.get(each.size() - 1), firstUniqueKey), range.getUpperBound());
                dumperContext.getCommonContext().setPosition(position);
                rowCount += each.size();
            }
        } finally {
            QuietlyCloser.close(calculateParam.getCalculationContext());
        }
        IngestPosition position = new IngestFinishedPosition();
        channel.push(Collections.singletonList(new FinishedRecord(position)));
        dumperContext.getCommonContext().setPosition(position);
        log.info("Dump by calculator done, rowCount={}, dataSource={}, table={}, initialPosition={}", rowCount, dumperContext.getCommonContext().getDataSourceName(), table, initialPosition);
    }
    
    private void dumpWithStreamingQuery() throws SQLException {
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        try (Connection connection = dataSource.getConnection()) {
            fetchAllNoUniqueKeyQuery(connection, databaseType, dumperContext.getBatchSize());
        }
    }
    
    private void fetchAllNoUniqueKeyQuery(final Connection connection, final DatabaseType databaseType, final int batchSize) throws SQLException {
        log.info("Start to fetch all no unique key query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
        try (PreparedStatement statement = JDBCStreamQueryBuilder.build(databaseType, connection, buildFetchAllNoUniqueKeySQL(), batchSize)) {
            runningStatement.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                consumeResultSetToChannel(resultSet, batchSize);
            } finally {
                runningStatement.set(null);
            }
        }
        log.info("End to fetch all no unique key query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
    }
    
    private String buildFetchAllNoUniqueKeySQL() {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        List<String> columnNames = dumperContext.getQueryColumnNames();
        return sqlBuilder.buildFetchAllSQL(schemaName, dumperContext.getActualTableName(), columnNames);
    }
    
    private void consumeResultSetToChannel(final ResultSet resultSet, final int batchSize) throws SQLException {
        long rowCount = 0;
        JobRateLimitAlgorithm rateLimitAlgorithm = dumperContext.getRateLimitAlgorithm();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<Record> dataRecords = new LinkedList<>();
        while (resultSet.next()) {
            if (dataRecords.size() >= batchSize) {
                channel.push(dataRecords);
                dataRecords = new LinkedList<>();
            }
            dataRecords.add(loadDataRecord(resultSet, resultSetMetaData));
            ++rowCount;
            if (!isRunning()) {
                log.info("Broke because of inventory dump is not running.");
                break;
            }
            if (null != rateLimitAlgorithm && 0 == rowCount % batchSize) {
                rateLimitAlgorithm.intercept(PipelineSQLOperationType.SELECT, 1);
            }
        }
        dataRecords.add(new FinishedRecord(new IngestFinishedPosition()));
        channel.push(dataRecords);
        log.info("Inventory dump with streaming query done, rowCount={}, dataSource={}, actualTable={}", rowCount, dumperContext.getCommonContext().getDataSourceName(),
                dumperContext.getActualTableName());
    }
    
    private DataRecord loadDataRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        String tableName = dumperContext.getLogicTableName();
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, tableName, positionCreator.create(dumperContext, resultSet), columnCount);
        List<String> insertColumnNames = Optional.ofNullable(dumperContext.getInsertColumnNames()).orElse(Collections.emptyList());
        ShardingSpherePreconditions.checkState(insertColumnNames.isEmpty() || insertColumnNames.size() == resultSetMetaData.getColumnCount(),
                () -> new PipelineInvalidParameterException("Insert column names count not equals ResultSet column count"));
        for (int i = 1; i <= columnCount; i++) {
            String columnName = insertColumnNames.isEmpty() ? resultSetMetaData.getColumnName(i) : insertColumnNames.get(i - 1);
            Column column = getColumn(resultSet, resultSetMetaData, columnName, i, dumperContext.getTargetUniqueKeysNames().contains(new ShardingSphereIdentifier(columnName)));
            result.addColumn(column);
        }
        result.setActualTableName(dumperContext.getActualTableName());
        return result;
    }
    
    private Column getColumn(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final String columnName, final int columnIndex, final boolean isUniqueKey) throws SQLException {
        return new NormalColumn(columnName, columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex), true, isUniqueKey);
    }
    
    @Override
    protected void doStop() {
        Optional.ofNullable(runningStatement.get()).ifPresent(PipelineJdbcUtils::cancelStatement);
    }
    
    private class RecordTableInventoryDumpCalculator extends AbstractRecordTableInventoryCalculator<List<DataRecord>, DataRecord> {
        
        RecordTableInventoryDumpCalculator(final int chunkSize, final StreamingRangeType streamingRangeType) {
            super(chunkSize, streamingRangeType);
        }
        
        @Override
        protected DataRecord readRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
            return loadDataRecord(resultSet, resultSetMetaData);
        }
        
        @Override
        protected Object getFirstUniqueKeyValue(final DataRecord record, final String firstUniqueKey) {
            return record.getColumn(firstUniqueKey).getValue();
        }
        
        @Override
        protected List<DataRecord> convertRecordsToResult(final List<DataRecord> records, final Object maxUniqueKeyValue) {
            return records;
        }
    }
}
