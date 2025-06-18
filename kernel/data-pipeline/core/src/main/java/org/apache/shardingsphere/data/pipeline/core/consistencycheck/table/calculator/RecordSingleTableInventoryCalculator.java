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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordSingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.query.JDBCStreamQueryBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineDataConsistencyCalculateSQLBuilder;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Record single table inventory calculator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class RecordSingleTableInventoryCalculator extends AbstractStreamingSingleTableInventoryCalculator {
    
    private final int chunkSize;
    
    private final int streamingChunkCount;
    
    private final EqualsBuilder equalsBuilder = new EqualsBuilder();
    
    public RecordSingleTableInventoryCalculator(final int chunkSize) {
        this.chunkSize = chunkSize;
        streamingChunkCount = 100;
    }
    
    @Override
    public Optional<SingleTableInventoryCalculatedResult> calculateChunk(final SingleTableInventoryCalculateParameter param) {
        List<Map<String, Object>> records = calculateChunk0(param);
        if (records.isEmpty()) {
            return Optional.empty();
        }
        String firstUniqueKey = param.getFirstUniqueKey().getName();
        if (QueryType.RANGE_QUERY == param.getQueryType()) {
            updateQueryRangeLower(param, records, firstUniqueKey);
        }
        return convertRecordsToResult(records, firstUniqueKey);
    }
    
    private List<Map<String, Object>> calculateChunk0(final SingleTableInventoryCalculateParameter param) {
        InventoryColumnValueReaderEngine columnValueReaderEngine = new InventoryColumnValueReaderEngine(param.getDatabaseType());
        try {
            if (QueryType.POINT_QUERY == param.getQueryType()) {
                return pointQuery(param, columnValueReaderEngine);
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
    
    private List<Map<String, Object>> pointQuery(final SingleTableInventoryCalculateParameter param, final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        List<Map<String, Object>> result = new LinkedList<>();
        CalculationContext calculationContext = prepareCalculationContext(param);
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            Map<String, Object> record = readRecord(columnValueReaderEngine, resultSet, resultSetMetaData);
            result.add(record);
        }
        return result;
    }
    
    private List<Map<String, Object>> rangeQueryWithSingleColumUniqueKey(final SingleTableInventoryCalculateParameter param,
                                                                         final InventoryColumnValueReaderEngine columnValueReaderEngine, final int round) throws SQLException {
        List<Map<String, Object>> result = new LinkedList<>();
        CalculationContext calculationContext = prepareCalculationContext(param);
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            result.add(readRecord(columnValueReaderEngine, resultSet, resultSetMetaData));
            if (result.size() == chunkSize) {
                return result;
            }
        }
        calculationContext.resetDatabaseResources();
        if (result.isEmpty() && 1 == round) {
            return rangeQueryWithSingleColumUniqueKey(param, columnValueReaderEngine, round + 1);
        }
        return result;
    }
    
    private List<Map<String, Object>> rangeQueryWithMultiColumUniqueKeys(final SingleTableInventoryCalculateParameter param,
                                                                         final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        CalculationContext calculationContext = prepareCalculationContext(param);
        if (calculationContext.getRecordDeque().size() > chunkSize) {
            return queryFromBuffer(calculationContext.getRecordDeque());
        }
        doRangeQueryWithMultiColumUniqueKeys(param, calculationContext, columnValueReaderEngine);
        return queryFromBuffer(calculationContext.getRecordDeque());
    }
    
    private void doRangeQueryWithMultiColumUniqueKeys(final SingleTableInventoryCalculateParameter param, final CalculationContext calculationContext,
                                                      final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        prepareDatabaseResources(calculationContext, param);
        ResultSet resultSet = calculationContext.getResultSet();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Object> previousRecord = calculationContext.getRecordDeque().pollLast();
        List<Map<String, Object>> duplicateRecords = new LinkedList<>();
        if (null != previousRecord) {
            duplicateRecords.add(previousRecord);
        }
        while (resultSet.next()) {
            ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
            Map<String, Object> record = readRecord(columnValueReaderEngine, resultSet, resultSetMetaData);
            if (null == previousRecord || DataConsistencyCheckUtils.isFirstUniqueKeyValueMatched(previousRecord, record, param.getFirstUniqueKey().getName(), equalsBuilder)) {
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
        calculationContext.resetDatabaseResources();
        if (!duplicateRecords.isEmpty()) {
            calculationContext.getRecordDeque().addAll(pointRangeQuery(param, duplicateRecords.get(0), columnValueReaderEngine));
        }
    }
    
    private List<Map<String, Object>> pointRangeQuery(final SingleTableInventoryCalculateParameter param, final Map<String, Object> duplicateRecord,
                                                      final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        Object duplicateUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(duplicateRecord, param.getFirstUniqueKey().getName());
        SingleTableInventoryCalculateParameter newParam = buildPointRangeQueryCalculateParameter(param, duplicateUniqueKeyValue);
        try {
            return pointQuery(newParam, columnValueReaderEngine);
        } finally {
            QuietlyCloser.close(newParam.getCalculationContext());
        }
    }
    
    private List<Map<String, Object>> queryFromBuffer(final Deque<Map<String, Object>> recordDeque) {
        List<Map<String, Object>> result = new LinkedList<>();
        while (true) {
            Map<String, Object> record = recordDeque.pollFirst();
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
    
    private CalculationContext prepareCalculationContext(final SingleTableInventoryCalculateParameter param) {
        CalculationContext result = (CalculationContext) param.getCalculationContext();
        if (null != result) {
            return result;
        }
        result = new CalculationContext();
        param.setCalculationContext(result);
        return result;
    }
    
    private void prepareDatabaseResources(final CalculationContext calculationContext, final SingleTableInventoryCalculateParameter param) throws SQLException {
        if (calculationContext.isDatabaseResourcesReady()) {
            return;
        }
        Connection connection = param.getDataSource().getConnection();
        calculationContext.setConnection(connection);
        String sql = getQuerySQL(param);
        PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(param.getDatabaseType(), connection, sql, chunkSize);
        setCurrentStatement(preparedStatement);
        calculationContext.setPreparedStatement(preparedStatement);
        setParameters(preparedStatement, param);
        ResultSet resultSet = preparedStatement.executeQuery();
        calculationContext.setResultSet(resultSet);
        calculationContext.setDatabaseResourcesReady(true);
    }
    
    private String getQuerySQL(final SingleTableInventoryCalculateParameter param) {
        ShardingSpherePreconditions.checkState(param.getUniqueKeys() != null && !param.getUniqueKeys().isEmpty() && null != param.getFirstUniqueKey(),
                () -> new UnsupportedOperationException("Record inventory calculator does not support table without unique key and primary key now."));
        PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder = new PipelineDataConsistencyCalculateSQLBuilder(param.getDatabaseType());
        Collection<String> columnNames = param.getColumnNames().isEmpty() ? Collections.singleton("*") : param.getColumnNames();
        switch (param.getQueryType()) {
            case RANGE_QUERY:
                return pipelineSQLBuilder.buildQueryRangeOrderingSQL(param.getTable(), columnNames, param.getUniqueKeysNames(), param.getQueryRange(), param.getShardingColumnsNames());
            case POINT_QUERY:
                return pipelineSQLBuilder.buildPointQuerySQL(param.getTable(), columnNames, param.getUniqueKeysNames(), param.getShardingColumnsNames());
            default:
                throw new UnsupportedOperationException("Query type: " + param.getQueryType());
        }
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final SingleTableInventoryCalculateParameter param) throws SQLException {
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
            preparedStatement.setObject(parameterIndex, chunkSize * streamingChunkCount);
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
    
    private SingleTableInventoryCalculateParameter buildPointRangeQueryCalculateParameter(final SingleTableInventoryCalculateParameter param, final Object uniqueKeyValue) {
        SingleTableInventoryCalculateParameter result = new SingleTableInventoryCalculateParameter(param.getDataSource(), param.getTable(), param.getColumnNames(),
                Collections.singletonList(param.getFirstUniqueKey()), QueryType.POINT_QUERY);
        result.setUniqueKeysValues(Collections.singletonList(uniqueKeyValue));
        result.setShardingColumnsNames(param.getShardingColumnsNames());
        result.setShardingColumnsValues(param.getShardingColumnsValues());
        return result;
    }
    
    private Map<String, Object> readRecord(final InventoryColumnValueReaderEngine columnValueReaderEngine, final ResultSet resultSet, final ResultSetMetaData resultSetMetaData) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int columnIndex = 1, columnCount = resultSetMetaData.getColumnCount(); columnIndex <= columnCount; columnIndex++) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex));
        }
        return result;
    }
    
    private void updateQueryRangeLower(final SingleTableInventoryCalculateParameter param, final List<Map<String, Object>> records, final String firstUniqueKey) {
        Object maxUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(records.size() - 1), firstUniqueKey);
        param.setQueryRange(new QueryRange(maxUniqueKeyValue, false, param.getQueryRange().getUpper()));
    }
    
    private Optional<SingleTableInventoryCalculatedResult> convertRecordsToResult(final List<Map<String, Object>> records, final String firstUniqueKey) {
        Object maxUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(records.size() - 1), firstUniqueKey);
        return Optional.of(new RecordSingleTableInventoryCalculatedResult(maxUniqueKeyValue, records));
    }
}
