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
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.InventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.InventoryQueryParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.point.InventoryPointQueryParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.InventoryRangeQueryParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPositionFactory;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.BuildDivisibleSQLParameter;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineInventoryDumpSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory dumper.
 */
@HighFrequencyInvocation
@Slf4j
public final class InventoryDumper extends AbstractPipelineLifecycleRunnable implements Dumper {
    
    private final InventoryDumperContext dumperContext;
    
    private final PipelineChannel channel;
    
    private final DataSource dataSource;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final InventoryDataRecordPositionCreator positionCreator;
    
    private final PipelineInventoryDumpSQLBuilder sqlBuilder;
    
    private final InventoryColumnValueReaderEngine columnValueReaderEngine;
    
    private final AtomicReference<Statement> runningStatement = new AtomicReference<>();
    
    public InventoryDumper(final InventoryDumperContext dumperContext, final PipelineChannel channel, final DataSource dataSource,
                           final PipelineTableMetaDataLoader metaDataLoader, final InventoryDataRecordPositionCreator positionCreator) {
        this.dumperContext = dumperContext;
        this.channel = channel;
        this.dataSource = dataSource;
        this.metaDataLoader = metaDataLoader;
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
        PipelineTableMetaData tableMetaData = getPipelineTableMetaData();
        try (Connection connection = dataSource.getConnection()) {
            if (StringUtils.isNotBlank(dumperContext.getQuerySQL()) || !dumperContext.hasUniqueKey() || isPrimaryKeyWithoutRange(position)) {
                dumpWithStreamingQuery(connection, tableMetaData);
            } else {
                dumpByPage(connection, tableMetaData);
            }
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Inventory dump failed on {}", dumperContext.getActualTableName(), ex);
            throw new IngestException("Inventory dump failed on " + dumperContext.getActualTableName(), ex);
        }
    }
    
    private PipelineTableMetaData getPipelineTableMetaData() {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        String tableName = dumperContext.getActualTableName();
        return metaDataLoader.getTableMetaData(schemaName, tableName);
    }
    
    private boolean isPrimaryKeyWithoutRange(final IngestPosition position) {
        return position instanceof PrimaryKeyIngestPosition && null == ((PrimaryKeyIngestPosition<?>) position).getBeginValue() && null == ((PrimaryKeyIngestPosition<?>) position).getEndValue();
    }
    
    @SuppressWarnings("MagicConstant")
    private void dumpByPage(final Connection connection, final PipelineTableMetaData tableMetaData) throws SQLException {
        log.info("Start to dump inventory data by page, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
        if (null != dumperContext.getTransactionIsolation()) {
            connection.setTransactionIsolation(dumperContext.getTransactionIsolation());
        }
        boolean firstQuery = true;
        AtomicLong rowCount = new AtomicLong();
        IngestPosition position = dumperContext.getCommonContext().getPosition();
        do {
            QueryRange queryRange = new QueryRange(((PrimaryKeyIngestPosition<?>) position).getBeginValue(), firstQuery && dumperContext.isFirstDump(),
                    ((PrimaryKeyIngestPosition<?>) position).getEndValue());
            InventoryQueryParameter<?> queryParam = new InventoryRangeQueryParameter(queryRange);
            List<Record> dataRecords = dumpByPage(connection, queryParam, rowCount, tableMetaData);
            if (dataRecords.size() > 1 && Objects.deepEquals(getFirstUniqueKeyValue(dataRecords, 0), getFirstUniqueKeyValue(dataRecords, dataRecords.size() - 1))) {
                queryParam = new InventoryPointQueryParameter(getFirstUniqueKeyValue(dataRecords, 0));
                dataRecords = dumpByPage(connection, queryParam, rowCount, tableMetaData);
            }
            firstQuery = false;
            if (dataRecords.isEmpty()) {
                position = new IngestFinishedPosition();
                dataRecords.add(new FinishedRecord(position));
                log.info("Inventory dump done, rowCount={}, dataSource={}, actualTable={}", rowCount, dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
            } else {
                position = PrimaryKeyIngestPositionFactory.newInstance(getFirstUniqueKeyValue(dataRecords, dataRecords.size() - 1), queryRange.getUpper());
            }
            channel.push(dataRecords);
            dumperContext.getCommonContext().setPosition(position);
        } while (!(position instanceof IngestFinishedPosition));
        log.info("End to dump inventory data by page, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
    }
    
    private List<Record> dumpByPage(final Connection connection,
                                    final InventoryQueryParameter<?> queryParam, final AtomicLong rowCount, final PipelineTableMetaData tableMetaData) throws SQLException {
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        int batchSize = dumperContext.getBatchSize();
        try (PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(databaseType, connection, buildDumpByPageSQL(queryParam), batchSize)) {
            runningStatement.set(preparedStatement);
            setParameters(preparedStatement, queryParam);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JobRateLimitAlgorithm rateLimitAlgorithm = dumperContext.getRateLimitAlgorithm();
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                List<Record> result = new LinkedList<>();
                while (resultSet.next()) {
                    if (result.size() >= batchSize) {
                        if (!dumperContext.hasUniqueKey()) {
                            channel.push(result);
                        }
                        result = new LinkedList<>();
                    }
                    result.add(loadDataRecord(resultSet, resultSetMetaData, tableMetaData));
                    rowCount.incrementAndGet();
                    if (!isRunning()) {
                        log.info("Broke because of inventory dump is not running.");
                        break;
                    }
                    if (null != rateLimitAlgorithm && 0 == rowCount.get() % batchSize) {
                        rateLimitAlgorithm.intercept(PipelineSQLOperationType.SELECT, 1);
                    }
                }
                return result;
            } finally {
                runningStatement.set(null);
            }
        }
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final InventoryQueryParameter<?> queryParam) throws SQLException {
        if (queryParam instanceof InventoryRangeQueryParameter) {
            int parameterIndex = 1;
            Object lower = ((InventoryRangeQueryParameter) queryParam).getValue().getLower();
            if (null != lower) {
                preparedStatement.setObject(parameterIndex++, lower);
            }
            Object upper = ((InventoryRangeQueryParameter) queryParam).getValue().getUpper();
            if (null != upper) {
                preparedStatement.setObject(parameterIndex++, upper);
            }
            preparedStatement.setInt(parameterIndex, dumperContext.getBatchSize());
        } else if (queryParam instanceof InventoryPointQueryParameter) {
            preparedStatement.setObject(1, queryParam.getValue());
        } else {
            throw new UnsupportedOperationException("Query type: " + queryParam.getValue());
        }
    }
    
    private DataRecord loadDataRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final PipelineTableMetaData tableMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        String tableName = dumperContext.getLogicTableName();
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, tableName, positionCreator.create(dumperContext, resultSet), columnCount);
        List<String> insertColumnNames = Optional.ofNullable(dumperContext.getInsertColumnNames()).orElse(Collections.emptyList());
        ShardingSpherePreconditions.checkState(insertColumnNames.isEmpty() || insertColumnNames.size() == resultSetMetaData.getColumnCount(),
                () -> new PipelineInvalidParameterException("Insert column names count not equals ResultSet column count"));
        for (int i = 1; i <= columnCount; i++) {
            String columnName = insertColumnNames.isEmpty() ? resultSetMetaData.getColumnName(i) : insertColumnNames.get(i - 1);
            ShardingSpherePreconditions.checkNotNull(tableMetaData.getColumnMetaData(columnName), () -> new PipelineInvalidParameterException(String.format("Column name is %s", columnName)));
            result.addColumn(new NormalColumn(columnName, columnValueReaderEngine.read(resultSet, resultSetMetaData, i), true, tableMetaData.getColumnMetaData(columnName).isUniqueKey()));
        }
        result.setActualTableName(dumperContext.getActualTableName());
        return result;
    }
    
    private String buildDumpByPageSQL(final InventoryQueryParameter<?> queryParam) {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        PipelineColumnMetaData firstColumn = dumperContext.getUniqueKeyColumns().get(0);
        List<String> columnNames = dumperContext.getQueryColumnNames();
        if (queryParam instanceof InventoryPointQueryParameter) {
            return sqlBuilder.buildPointQuerySQL(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName());
        }
        QueryRange queryRange = ((InventoryRangeQueryParameter) queryParam).getValue();
        boolean lowerInclusive = queryRange.isLowerInclusive();
        if (null != queryRange.getLower() && null != queryRange.getUpper()) {
            return sqlBuilder.buildDivisibleSQL(new BuildDivisibleSQLParameter(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName(), lowerInclusive, true));
        }
        if (null != queryRange.getLower()) {
            return sqlBuilder.buildDivisibleSQL(new BuildDivisibleSQLParameter(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName(), lowerInclusive, false));
        }
        throw new PipelineInternalException("Primary key position is invalid.");
    }
    
    private Object getFirstUniqueKeyValue(final List<Record> dataRecords, final int index) {
        return ((DataRecord) dataRecords.get(index)).getUniqueKeyValue().iterator().next();
    }
    
    @SuppressWarnings("MagicConstant")
    private void dumpWithStreamingQuery(final Connection connection, final PipelineTableMetaData tableMetaData) throws SQLException {
        int batchSize = dumperContext.getBatchSize();
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        if (null != dumperContext.getTransactionIsolation()) {
            connection.setTransactionIsolation(dumperContext.getTransactionIsolation());
        }
        if (null == dumperContext.getQuerySQL()) {
            fetchAllQuery(connection, tableMetaData, databaseType, batchSize);
        } else {
            designatedParametersQuery(connection, tableMetaData, databaseType, batchSize);
        }
    }
    
    private void fetchAllQuery(final Connection connection, final PipelineTableMetaData tableMetaData, final DatabaseType databaseType,
                               final int batchSize) throws SQLException {
        log.info("Start to fetch all inventory data with streaming query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
        try (PreparedStatement statement = JDBCStreamQueryBuilder.build(databaseType, connection, buildFetchAllSQLWithStreamingQuery(), batchSize)) {
            runningStatement.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                consumeResultSetToChannel(tableMetaData, resultSet, batchSize);
            } finally {
                runningStatement.set(null);
            }
        }
        log.info("End to fetch all inventory data with streaming query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
    }
    
    private void designatedParametersQuery(final Connection connection, final PipelineTableMetaData tableMetaData, final DatabaseType databaseType, final int batchSize) throws SQLException {
        log.info("Start to dump inventory data with designated parameters query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(),
                dumperContext.getActualTableName());
        try (PreparedStatement statement = JDBCStreamQueryBuilder.build(databaseType, connection, dumperContext.getQuerySQL(), batchSize)) {
            runningStatement.set(statement);
            for (int i = 0; i < dumperContext.getQueryParams().size(); i++) {
                statement.setObject(i + 1, dumperContext.getQueryParams().get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                consumeResultSetToChannel(tableMetaData, resultSet, batchSize);
            } finally {
                runningStatement.set(null);
            }
        }
        log.info("End to dump inventory data with designated parameters query, dataSource={}, actualTable={}", dumperContext.getCommonContext().getDataSourceName(),
                dumperContext.getActualTableName());
    }
    
    private void consumeResultSetToChannel(final PipelineTableMetaData tableMetaData, final ResultSet resultSet, final int batchSize) throws SQLException {
        int rowCount = 0;
        JobRateLimitAlgorithm rateLimitAlgorithm = dumperContext.getRateLimitAlgorithm();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<Record> dataRecords = new LinkedList<>();
        while (resultSet.next()) {
            if (dataRecords.size() >= batchSize) {
                channel.push(dataRecords);
                dataRecords = new LinkedList<>();
            }
            dataRecords.add(loadDataRecord(resultSet, resultSetMetaData, tableMetaData));
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
    
    private String buildFetchAllSQLWithStreamingQuery() {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        List<String> columnNames = dumperContext.getQueryColumnNames();
        if (dumperContext.hasUniqueKey()) {
            return sqlBuilder.buildFetchAllSQL(schemaName, dumperContext.getActualTableName(), columnNames, dumperContext.getUniqueKeyColumns().get(0).getName());
        }
        return sqlBuilder.buildFetchAllSQL(schemaName, dumperContext.getActualTableName(), columnNames);
    }
    
    @Override
    protected void doStop() {
        Optional.ofNullable(runningStatement.get()).ifPresent(PipelineJdbcUtils::cancelStatement);
    }
}
