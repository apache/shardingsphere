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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordSingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Record single table inventory calculator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class RecordSingleTableInventoryCalculator extends AbstractStreamingSingleTableInventoryCalculator {
    
    private final int chunkSize;
    
    @Override
    public Optional<SingleTableInventoryCalculatedResult> calculateChunk(final SingleTableInventoryCalculateParameter param) {
        List<Map<String, Object>> records = calculateChunk(param, QueryType.RANGE_QUERY == param.getQueryType());
        if (records.isEmpty()) {
            return Optional.empty();
        }
        String firstUniqueKey = param.getFirstUniqueKey().getName();
        if (QueryType.POINT_QUERY == param.getQueryType()) {
            return convertRecordsToResult(records, firstUniqueKey);
        }
        if (records.size() == chunkSize) {
            Object minUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(0), firstUniqueKey);
            removeLastRecords(records, param);
            if (!records.isEmpty()) {
                updateQueryRangeLower(param, records, firstUniqueKey);
                return convertRecordsToResult(records, firstUniqueKey);
            }
            SingleTableInventoryCalculateParameter newParam = buildNewCalculateParameter(param, minUniqueKeyValue);
            records = calculateChunk(newParam, false);
            if (!records.isEmpty()) {
                updateQueryRangeLower(param, records, firstUniqueKey);
                return convertRecordsToResult(records, firstUniqueKey);
            }
            return Optional.empty();
        }
        updateQueryRangeLower(param, records, firstUniqueKey);
        return convertRecordsToResult(records, firstUniqueKey);
    }
    
    private List<Map<String, Object>> calculateChunk(final SingleTableInventoryCalculateParameter param, final boolean isRangeQuery) {
        try (CalculationContext calculationContext = getOrCreateCalculationContext(param)) {
            List<Map<String, Object>> result = new LinkedList<>();
            InventoryColumnValueReaderEngine columnValueReaderEngine = new InventoryColumnValueReaderEngine(param.getDatabaseType());
            ResultSet resultSet = calculationContext.getResultSet();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                ShardingSpherePreconditions.checkState(!isCanceling(), () -> new PipelineJobCancelingException("Calculate chunk canceled, qualified table: %s", param.getTable()));
                Map<String, Object> columnRecord = new LinkedHashMap<>();
                for (int columnIndex = 1, columnCount = resultSetMetaData.getColumnCount(); columnIndex <= columnCount; columnIndex++) {
                    columnRecord.put(resultSetMetaData.getColumnLabel(columnIndex), columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex));
                }
                result.add(columnRecord);
                if (isRangeQuery && result.size() == chunkSize) {
                    break;
                }
            }
            return result;
        } catch (final PipelineSQLException | PipelineJobCancelingException ex) {
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), ex);
        }
    }
    
    private CalculationContext getOrCreateCalculationContext(final SingleTableInventoryCalculateParameter param) {
        CalculationContext result = (CalculationContext) param.getCalculationContext();
        if (null != result && !result.isClosed()) {
            return result;
        }
        try {
            result = createCalculationContext(param);
            fulfillCalculationContext(result, param);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            QuietlyCloser.close(result);
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getTable(), ex);
        }
        return result;
    }
    
    private CalculationContext createCalculationContext(final SingleTableInventoryCalculateParameter param) throws SQLException {
        Connection connection = param.getDataSource().getConnection();
        CalculationContext result = new CalculationContext();
        result.setConnection(connection);
        param.setCalculationContext(result);
        return result;
    }
    
    private void fulfillCalculationContext(final CalculationContext calculationContext, final SingleTableInventoryCalculateParameter param) throws SQLException {
        String sql = getQuerySQL(param);
        PreparedStatement preparedStatement = JDBCStreamQueryBuilder.build(param.getDatabaseType(), calculationContext.getConnection(), sql, chunkSize);
        setCurrentStatement(preparedStatement);
        calculationContext.setPreparedStatement(preparedStatement);
        setParameters(preparedStatement, param);
        ResultSet resultSet = preparedStatement.executeQuery();
        calculationContext.setResultSet(resultSet);
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
            preparedStatement.setObject(parameterIndex, chunkSize);
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
    
    private void removeLastRecords(final List<Map<String, Object>> records, final SingleTableInventoryCalculateParameter param) {
        Object minUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(0), param.getFirstUniqueKey().getName());
        Object maxUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(records.size() - 1), param.getFirstUniqueKey().getName());
        if (Objects.equals(minUniqueKeyValue, maxUniqueKeyValue)) {
            records.clear();
            return;
        }
        records.remove(records.size() - 1);
        for (int i = records.size() - 1; i >= 0; i--) {
            if (Objects.deepEquals(maxUniqueKeyValue, DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(i), param.getFirstUniqueKey().getName()))) {
                records.remove(i);
            } else {
                break;
            }
        }
    }
    
    private Optional<SingleTableInventoryCalculatedResult> convertRecordsToResult(final List<Map<String, Object>> records, final String firstUniqueKey) {
        Object maxUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(records.size() - 1), firstUniqueKey);
        return Optional.of(new RecordSingleTableInventoryCalculatedResult(maxUniqueKeyValue, records));
    }
    
    private SingleTableInventoryCalculateParameter buildNewCalculateParameter(final SingleTableInventoryCalculateParameter param, final Object minUniqueKeyValue) {
        SingleTableInventoryCalculateParameter result = new SingleTableInventoryCalculateParameter(param.getDataSource(), param.getTable(), param.getColumnNames(),
                Collections.singletonList(param.getFirstUniqueKey()), QueryType.POINT_QUERY);
        result.setUniqueKeysValues(Collections.singletonList(minUniqueKeyValue));
        result.setShardingColumnsNames(param.getShardingColumnsNames());
        result.setShardingColumnsValues(param.getShardingColumnsValues());
        return result;
    }
    
    private void updateQueryRangeLower(final SingleTableInventoryCalculateParameter param, final List<Map<String, Object>> records, final String firstUniqueKey) {
        Object maxUniqueKeyValue = DataConsistencyCheckUtils.getFirstUniqueKeyValue(records.get(records.size() - 1), firstUniqueKey);
        param.setQueryRange(new QueryRange(maxUniqueKeyValue, false, param.getQueryRange().getUpper()));
    }
}
