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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.PipelineDatabaseResources;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.StreamingRangeType;
import org.apache.shardingsphere.data.pipeline.core.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineInventoryCalculateSQLBuilder;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract record table inventory calculator.
 *
 * @param <S> the type of result
 * @param <C> the type of record
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public abstract class AbstractRecordTableInventoryCalculator<S, C> extends AbstractStreamingTableInventoryCalculator<S> {
    
    private static final int DEFAULT_STREAMING_CHUNK_COUNT = 100;
    
    private final int chunkSize;
    
    private final int streamingChunkCount;
    
    private final StreamingRangeType streamingRangeType;
    
    protected AbstractRecordTableInventoryCalculator(final int chunkSize, final StreamingRangeType streamingRangeType) {
        this(chunkSize, DEFAULT_STREAMING_CHUNK_COUNT, streamingRangeType);
    }
    
    @Override
    public Optional<S> calculateChunk(final TableInventoryCalculateParameter param) {
        List<C> records = calculateChunk0(param);
        if (records.isEmpty()) {
            return Optional.empty();
        }
        Object maxUniqueKeyValue = getFirstUniqueKeyValue(records.get(records.size() - 1), param.getFirstUniqueKey().getName());
        if (QueryType.RANGE_QUERY == param.getQueryType()) {
            param.setQueryRange(new QueryRange(maxUniqueKeyValue, false, param.getQueryRange().getUpper()));
        }
        return Optional.of(convertRecordsToResult(records, maxUniqueKeyValue));
    }
    
    private List<C> calculateChunk0(final TableInventoryCalculateParameter param) {
        InventoryColumnValueReaderEngine columnValueReaderEngine = new InventoryColumnValueReaderEngine(param.getDatabaseType());
        try {
            if (QueryType.POINT_QUERY == param.getQueryType()) {
                return pointQuery(param, columnValueReaderEngine);
            }
            if (StreamingRangeType.LARGE == streamingRangeType) {
                return allQuery(param, columnValueReaderEngine);
            }
            if (param.getUniqueKeys().size() <= 1) {
                return rangeQueryWithSingleColumUniqueKey(param, columnValueReaderEngine, 1);
            }
            return rangeQueryWithMultiColumUniqueKeys(param, columnValueReaderEngine);
        } catch (final PipelineSQLException | PipelineJobCancelingException ex) {
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), ex);
        }
    }
    
    private List<C> pointQuery(final TableInventoryCalculateParameter param, final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        List<C> result = new LinkedList<>();
        CalculationContext<C> calculationContext = prepareCalculationContext(param);
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getDatabaseResources().getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            C record = readRecord(resultSet, resultSetMetaData, columnValueReaderEngine);
            result.add(record);
        }
        return result;
    }
    
    private List<C> allQuery(final TableInventoryCalculateParameter param, final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        List<C> result = new LinkedList<>();
        CalculationContext<C> calculationContext = prepareCalculationContext(param);
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getDatabaseResources().getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            result.add(readRecord(resultSet, resultSetMetaData, columnValueReaderEngine));
            if (result.size() == chunkSize) {
                break;
            }
        }
        if (result.isEmpty()) {
            QuietlyCloser.close(calculationContext);
        }
        return result;
    }
    
    private List<C> rangeQueryWithSingleColumUniqueKey(final TableInventoryCalculateParameter param,
                                                       final InventoryColumnValueReaderEngine columnValueReaderEngine, final int round) throws SQLException {
        List<C> result = new LinkedList<>();
        CalculationContext<C> calculationContext = prepareCalculationContext(param);
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getDatabaseResources().getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            result.add(readRecord(resultSet, resultSetMetaData, columnValueReaderEngine));
            if (result.size() == chunkSize) {
                return result;
            }
        }
        calculationContext.getDatabaseResources().reset();
        if (result.isEmpty() && 1 == round) {
            return rangeQueryWithSingleColumUniqueKey(param, columnValueReaderEngine, round + 1);
        }
        return result;
    }
    
    private List<C> rangeQueryWithMultiColumUniqueKeys(final TableInventoryCalculateParameter param,
                                                       final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        CalculationContext<C> calculationContext = prepareCalculationContext(param);
        if (calculationContext.getRecordDeque().size() > chunkSize) {
            return queryFromBuffer(calculationContext.getRecordDeque());
        }
        doRangeQueryWithMultiColumUniqueKeys(param, calculationContext, columnValueReaderEngine);
        return queryFromBuffer(calculationContext.getRecordDeque());
    }
    
    private void doRangeQueryWithMultiColumUniqueKeys(final TableInventoryCalculateParameter param, final CalculationContext<C> calculationContext,
                                                      final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getDatabaseResources().getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        C previousRecord = calculationContext.getRecordDeque().pollLast();
        List<C> duplicateRecords = new LinkedList<>();
        if (null != previousRecord) {
            duplicateRecords.add(previousRecord);
        }
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        String firstUniqueKey = param.getFirstUniqueKey().getName();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            C record = readRecord(resultSet, resultSetMetaData, columnValueReaderEngine);
            if (null == previousRecord || DataConsistencyCheckUtils.isMatched(equalsBuilder,
                    getFirstUniqueKeyValue(previousRecord, firstUniqueKey), getFirstUniqueKeyValue(record, firstUniqueKey))) {
                duplicateRecords.add(record);
                previousRecord = record;
                continue;
            }
            previousRecord = record;
            if (!duplicateRecords.isEmpty()) {
                calculationContext.getRecordDeque().addAll(duplicateRecords);
                duplicateRecords.clear();
            }
            if (calculationContext.getRecordDeque().size() >= chunkSize) {
                calculationContext.getRecordDeque().add(record);
                return;
            }
            duplicateRecords.add(record);
        }
        calculationContext.getDatabaseResources().reset();
        if (!duplicateRecords.isEmpty()) {
            calculationContext.getRecordDeque().addAll(pointRangeQuery(param, duplicateRecords.get(0), columnValueReaderEngine));
        }
    }
    
    private List<C> pointRangeQuery(final TableInventoryCalculateParameter param, final C duplicateRecord,
                                    final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        Object duplicateUniqueKeyValue = getFirstUniqueKeyValue(duplicateRecord, param.getFirstUniqueKey().getName());
        TableInventoryCalculateParameter newParam = buildPointRangeQueryCalculateParameter(param, duplicateUniqueKeyValue);
        try {
            return pointQuery(newParam, columnValueReaderEngine);
        } finally {
            QuietlyCloser.close(newParam.getCalculationContext());
        }
    }
    
    private List<C> queryFromBuffer(final Deque<C> recordDeque) {
        List<C> result = new LinkedList<>();
        while (true) {
            C record = recordDeque.pollFirst();
            if (null == record) {
                break;
            }
            result.add(record);
            if (result.size() == chunkSize) {
                break;
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private CalculationContext<C> prepareCalculationContext(final TableInventoryCalculateParameter param) {
        CalculationContext<C> result = (CalculationContext<C>) param.getCalculationContext();
        if (null != result) {
            return result;
        }
        result = new CalculationContext<>();
        param.setCalculationContext(result);
        return result;
    }
    
    private void prepareDatabaseResources(final CalculationContext<C> calculationContext, final TableInventoryCalculateParameter param) throws SQLException {
        if (calculationContext.getDatabaseResources().isReady()) {
            return;
        }
        PipelineDatabaseResources databaseResources = calculationContext.getDatabaseResources();
        Connection connection = param.getDataSource().getConnection();
        databaseResources.setConnection(connection);
        String sql = getQuerySQL(param);
        PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(param.getDatabaseType(), connection, sql, chunkSize);
        setCurrentStatement(preparedStatement);
        databaseResources.setPreparedStatement(preparedStatement);
        setParameters(preparedStatement, param);
        ResultSet resultSet = preparedStatement.executeQuery();
        databaseResources.setResultSet(resultSet);
        databaseResources.setReady(true);
    }
    
    private String getQuerySQL(final TableInventoryCalculateParameter param) {
        ShardingSpherePreconditions.checkState(null != param.getUniqueKeys() && !param.getUniqueKeys().isEmpty() && null != param.getFirstUniqueKey(),
                () -> new UnsupportedOperationException("Record inventory calculator does not support table without unique key and primary key now."));
        PipelineInventoryCalculateSQLBuilder pipelineSQLBuilder = new PipelineInventoryCalculateSQLBuilder(param.getDatabaseType());
        Collection<String> columnNames = param.getColumnNames().isEmpty() ? Collections.singleton("*") : param.getColumnNames();
        switch (param.getQueryType()) {
            case RANGE_QUERY:
                return pipelineSQLBuilder.buildQueryRangeOrderingSQL(param.getTable(), columnNames, param.getUniqueKeysNames(), param.getQueryRange(),
                        StreamingRangeType.SMALL == streamingRangeType, param.getShardingColumnsNames());
            case POINT_QUERY:
                return pipelineSQLBuilder.buildPointQuerySQL(param.getTable(), columnNames, param.getUniqueKeysNames(), param.getShardingColumnsNames());
            default:
                throw new UnsupportedOperationException("Query type: " + param.getQueryType());
        }
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final TableInventoryCalculateParameter param) throws SQLException {
        QueryType queryType = param.getQueryType();
        if (queryType == QueryType.RANGE_QUERY) {
            QueryRange queryRange = param.getQueryRange();
            ShardingSpherePreconditions.checkNotNull(queryRange,
                    () -> new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), new RuntimeException("Unique keys values range is null.")));
            int parameterIndex = 1;
            if (null != queryRange.getLower()) {
                preparedStatement.setObject(parameterIndex++, queryRange.getLower());
            }
            if (null != queryRange.getUpper()) {
                preparedStatement.setObject(parameterIndex++, queryRange.getUpper());
            }
            if (StreamingRangeType.SMALL == streamingRangeType) {
                preparedStatement.setObject(parameterIndex, chunkSize * streamingChunkCount);
            }
        } else if (queryType == QueryType.POINT_QUERY) {
            Collection<Object> uniqueKeysValues = param.getUniqueKeysValues();
            ShardingSpherePreconditions.checkNotNull(uniqueKeysValues,
                    () -> new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), new RuntimeException("Unique keys values is null.")));
            int parameterIndex = 1;
            for (Object each : uniqueKeysValues) {
                preparedStatement.setObject(parameterIndex++, each);
            }
            if (null != param.getShardingColumnsNames() && !param.getShardingColumnsNames().isEmpty()) {
                List<Object> shardingColumnsValues = param.getShardingColumnsValues();
                ShardingSpherePreconditions.checkNotNull(shardingColumnsValues,
                        () -> new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), new RuntimeException("Sharding columns values is null when names not empty.")));
                for (Object each : shardingColumnsValues) {
                    preparedStatement.setObject(parameterIndex++, each);
                }
            }
        } else {
            throw new UnsupportedOperationException("Query type: " + queryType);
        }
    }
    
    private TableInventoryCalculateParameter buildPointRangeQueryCalculateParameter(final TableInventoryCalculateParameter param, final Object uniqueKeyValue) {
        TableInventoryCalculateParameter result = new TableInventoryCalculateParameter(param.getDataSource(), param.getTable(), param.getColumnNames(),
                Collections.singletonList(param.getFirstUniqueKey()), QueryType.POINT_QUERY, param.getQueryCondition());
        result.setUniqueKeysValues(Collections.singletonList(uniqueKeyValue));
        result.setShardingColumnsNames(param.getShardingColumnsNames());
        result.setShardingColumnsValues(param.getShardingColumnsValues());
        return result;
    }
    
    protected abstract C readRecord(ResultSet resultSet, ResultSetMetaData resultSetMetaData, InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException;
    
    protected abstract Object getFirstUniqueKeyValue(C record, String firstUniqueKey);
    
    protected abstract S convertRecordsToResult(List<C> records, Object maxUniqueKeyValue);
}
