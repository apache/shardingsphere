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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPositionFactory;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineInventoryDumpSQLBuilder;
import org.apache.shardingsphere.infra.util.DatabaseTypeUtils;
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
public class InventoryDumper extends AbstractPipelineLifecycleRunnable implements Dumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final InventoryDumperContext dumperContext;
    
    private final PipelineChannel channel;
    
    private final DataSource dataSource;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final PipelineInventoryDumpSQLBuilder inventoryDumpSQLBuilder;
    
    private final InventoryColumnValueReaderEngine columnValueReaderEngine;
    
    private final AtomicReference<Statement> runningStatement = new AtomicReference<>();
    
    private PipelineTableMetaData tableMetaData;
    
    public InventoryDumper(final InventoryDumperContext dumperContext, final PipelineChannel channel, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperContext = dumperContext;
        this.channel = channel;
        this.dataSource = dataSource;
        this.metaDataLoader = metaDataLoader;
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        inventoryDumpSQLBuilder = new PipelineInventoryDumpSQLBuilder(databaseType);
        columnValueReaderEngine = new InventoryColumnValueReaderEngine(databaseType);
    }
    
    @Override
    protected void runBlocking() {
        IngestPosition position = dumperContext.getCommonContext().getPosition();
        if (position instanceof IngestFinishedPosition) {
            log.info("Ignored because of already finished.");
            return;
        }
        init();
        try (Connection connection = dataSource.getConnection()) {
            if (Strings.isNullOrEmpty(dumperContext.getQuerySQL()) && dumperContext.hasUniqueKey() && !isPrimaryKeyWithoutRange(position)) {
                dumpPageByPage(connection);
            } else {
                dumpWithStreamingQuery(connection);
            }
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Inventory dump failed on {}", dumperContext.getActualTableName(), ex);
            throw new IngestException("Inventory dump failed on " + dumperContext.getActualTableName(), ex);
        }
    }
    
    private void init() {
        if (null == tableMetaData) {
            tableMetaData = metaDataLoader.getTableMetaData(
                    dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName()), dumperContext.getActualTableName());
        }
    }
    
    private boolean isPrimaryKeyWithoutRange(final IngestPosition position) {
        return position instanceof PrimaryKeyIngestPosition && null == ((PrimaryKeyIngestPosition<?>) position).getBeginValue() && null == ((PrimaryKeyIngestPosition<?>) position).getEndValue();
    }
    
    @SuppressWarnings("MagicConstant")
    private void dumpPageByPage(final Connection connection) throws SQLException {
        if (null != dumperContext.getTransactionIsolation()) {
            connection.setTransactionIsolation(dumperContext.getTransactionIsolation());
        }
        boolean firstQuery = true;
        AtomicLong rowCount = new AtomicLong();
        IngestPosition position = dumperContext.getCommonContext().getPosition();
        while (true) {
            QueryRange queryRange = new QueryRange(((PrimaryKeyIngestPosition<?>) position).getBeginValue(), firstQuery, ((PrimaryKeyIngestPosition<?>) position).getEndValue());
            InventoryQueryParameter queryParam = InventoryQueryParameter.buildForRangeQuery(queryRange);
            List<Record> dataRecords = dumpPageByPage(connection, queryParam, rowCount);
            if (dataRecords.size() > 1 && Objects.deepEquals(getFirstUniqueKeyValue(dataRecords, 0), getFirstUniqueKeyValue(dataRecords, dataRecords.size() - 1))) {
                queryParam = InventoryQueryParameter.buildForPointQuery(getFirstUniqueKeyValue(dataRecords, 0));
                dataRecords = dumpPageByPage(connection, queryParam, rowCount);
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
            if (position instanceof IngestFinishedPosition) {
                break;
            }
        }
    }
    
    private List<Record> dumpPageByPage(final Connection connection, final InventoryQueryParameter queryParam, final AtomicLong rowCount) throws SQLException {
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        int batchSize = dumperContext.getBatchSize();
        try (PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(databaseType, connection, buildInventoryDumpPageByPageSQL(queryParam))) {
            runningStatement.set(preparedStatement);
            if (!DatabaseTypeUtils.isMySQL(databaseType)) {
                preparedStatement.setFetchSize(batchSize);
            }
            setParameters(preparedStatement, queryParam, false);
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
                    result.add(loadDataRecord(resultSet, resultSetMetaData));
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
    
    private void setParameters(final PreparedStatement preparedStatement, final InventoryQueryParameter queryParam, final boolean streamingQuery) throws SQLException {
        if (!Strings.isNullOrEmpty(dumperContext.getQuerySQL())) {
            for (int i = 0; i < dumperContext.getQueryParams().size(); i++) {
                preparedStatement.setObject(i + 1, dumperContext.getQueryParams().get(i));
            }
            return;
        }
        if (!dumperContext.hasUniqueKey()) {
            return;
        }
        int parameterIndex = 1;
        if (QueryType.RANGE_QUERY == queryParam.getQueryType()) {
            Object lower = queryParam.getUniqueKeyValueRange().getLower();
            if (null != lower) {
                preparedStatement.setObject(parameterIndex++, lower);
            }
            Object upper = queryParam.getUniqueKeyValueRange().getUpper();
            if (null != upper) {
                preparedStatement.setObject(parameterIndex++, upper);
            }
            if (!streamingQuery) {
                preparedStatement.setInt(parameterIndex, dumperContext.getBatchSize());
            }
        } else if (QueryType.POINT_QUERY == queryParam.getQueryType()) {
            preparedStatement.setObject(parameterIndex, queryParam.getUniqueKeyValue());
        } else {
            throw new UnsupportedOperationException("Query type: " + queryParam.getQueryType());
        }
    }
    
    private DataRecord loadDataRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, dumperContext.getLogicTableName(), newDataRecordPosition(resultSet), columnCount);
        List<String> insertColumnNames = Optional.ofNullable(dumperContext.getInsertColumnNames()).orElse(Collections.emptyList());
        ShardingSpherePreconditions.checkState(insertColumnNames.isEmpty() || insertColumnNames.size() == resultSetMetaData.getColumnCount(),
                () -> new PipelineInvalidParameterException("Insert column names count not equals ResultSet column count"));
        for (int i = 1; i <= columnCount; i++) {
            String columnName = insertColumnNames.isEmpty() ? resultSetMetaData.getColumnName(i) : insertColumnNames.get(i - 1);
            ShardingSpherePreconditions.checkNotNull(tableMetaData.getColumnMetaData(columnName), () -> new PipelineInvalidParameterException(String.format("Column name is %s", columnName)));
            result.addColumn(new Column(columnName, columnValueReaderEngine.read(resultSet, resultSetMetaData, i), true, tableMetaData.getColumnMetaData(columnName).isUniqueKey()));
        }
        result.setActualTableName(dumperContext.getActualTableName());
        return result;
    }
    
    protected IngestPosition newDataRecordPosition(final ResultSet resultSet) throws SQLException {
        return dumperContext.hasUniqueKey()
                ? PrimaryKeyIngestPositionFactory.newInstance(
                        resultSet.getObject(dumperContext.getUniqueKeyColumns().get(0).getName()), ((PrimaryKeyIngestPosition<?>) dumperContext.getCommonContext().getPosition()).getEndValue())
                : new IngestPlaceholderPosition();
    }
    
    private String buildInventoryDumpPageByPageSQL(final InventoryQueryParameter queryParam) {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        PipelineColumnMetaData firstColumn = dumperContext.getUniqueKeyColumns().get(0);
        List<String> columnNames = getQueryColumnNames();
        if (QueryType.POINT_QUERY == queryParam.getQueryType()) {
            return inventoryDumpSQLBuilder.buildPointQuerySQL(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName());
        }
        QueryRange queryRange = queryParam.getUniqueKeyValueRange();
        boolean lowerInclusive = queryRange.isLowerInclusive();
        if (null != queryRange.getLower() && null != queryRange.getUpper()) {
            return inventoryDumpSQLBuilder.buildDivisibleSQL(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName(), lowerInclusive, true);
        }
        if (null != queryRange.getLower()) {
            return inventoryDumpSQLBuilder.buildDivisibleSQL(schemaName, dumperContext.getActualTableName(), columnNames, firstColumn.getName(), lowerInclusive, false);
        }
        throw new PipelineInternalException("Primary key position is invalid.");
    }
    
    private List<String> getQueryColumnNames() {
        return Optional.ofNullable(dumperContext.getInsertColumnNames()).orElse(Collections.singletonList("*"));
    }
    
    private Object getFirstUniqueKeyValue(final List<Record> dataRecords, final int index) {
        return ((DataRecord) dataRecords.get(index)).getUniqueKeyValue().iterator().next();
    }
    
    @SuppressWarnings("MagicConstant")
    private void dumpWithStreamingQuery(final Connection connection) throws SQLException {
        int batchSize = dumperContext.getBatchSize();
        DatabaseType databaseType = dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType();
        if (null != dumperContext.getTransactionIsolation()) {
            connection.setTransactionIsolation(dumperContext.getTransactionIsolation());
        }
        try (PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(databaseType, connection, buildInventoryDumpSQLWithStreamingQuery())) {
            runningStatement.set(preparedStatement);
            if (!DatabaseTypeUtils.isMySQL(databaseType)) {
                preparedStatement.setFetchSize(batchSize);
            }
            PrimaryKeyIngestPosition<?> primaryPosition = (PrimaryKeyIngestPosition<?>) dumperContext.getCommonContext().getPosition();
            InventoryQueryParameter queryParam = InventoryQueryParameter.buildForRangeQuery(new QueryRange(primaryPosition.getBeginValue(), true, primaryPosition.getEndValue()));
            setParameters(preparedStatement, queryParam, true);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                int rowCount = 0;
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
                log.info("Inventory dump with streaming query done, rowCount={}, dataSource={}, actualTable={}",
                        rowCount, dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName());
            } finally {
                runningStatement.set(null);
            }
        }
    }
    
    private String buildInventoryDumpSQLWithStreamingQuery() {
        if (!Strings.isNullOrEmpty(dumperContext.getQuerySQL())) {
            return dumperContext.getQuerySQL();
        }
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        List<String> columnNames = getQueryColumnNames();
        return inventoryDumpSQLBuilder.buildFetchAllSQL(schemaName, dumperContext.getActualTableName(), columnNames);
    }
    
    @Override
    protected void doStop() {
        Optional.ofNullable(runningStatement.get()).ifPresent(PipelineJdbcUtils::cancelStatement);
    }
}
