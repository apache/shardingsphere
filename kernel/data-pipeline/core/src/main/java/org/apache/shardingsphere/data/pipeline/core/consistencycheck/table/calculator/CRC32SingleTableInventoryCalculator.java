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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineDataConsistencyCalculateSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedCRC32SingleTableInventoryCalculatorException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRC32 single table inventory calculator.
 */
@Slf4j
public final class CRC32SingleTableInventoryCalculator extends AbstractSingleTableInventoryCalculator {
    
    @Override
    public Iterable<SingleTableInventoryCalculatedResult> calculate(final SingleTableInventoryCalculateParameter param) {
        PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder = new PipelineDataConsistencyCalculateSQLBuilder(param.getDatabaseType());
        List<CalculatedItem> calculatedItems = param.getColumnNames().stream().map(each -> calculateCRC32(pipelineSQLBuilder, param, each)).collect(Collectors.toList());
        return Collections.singletonList(new CalculatedResult(calculatedItems.get(0).getRecordsCount(), calculatedItems.stream().map(CalculatedItem::getCrc32).collect(Collectors.toList())));
    }
    
    private CalculatedItem calculateCRC32(final PipelineDataConsistencyCalculateSQLBuilder pipelineSQLBuilder, final SingleTableInventoryCalculateParameter param, final String columnName) {
        Optional<String> sql = pipelineSQLBuilder.buildCRC32SQL(param.getSchemaName(), param.getLogicTableName(), columnName);
        ShardingSpherePreconditions.checkState(sql.isPresent(), () -> new UnsupportedCRC32SingleTableInventoryCalculatorException(param.getDatabaseType()));
        try (
                Connection connection = param.getDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql.get());
                ResultSet resultSet = preparedStatement.executeQuery()) {
            setCurrentStatement(preparedStatement);
            resultSet.next();
            long crc32 = resultSet.getLong(1);
            int recordsCount = resultSet.getInt(2);
            return new CalculatedItem(crc32, recordsCount);
        } catch (final SQLException ex) {
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(param.getSchemaName(), param.getLogicTableName(), ex);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedItem {
        
        private final long crc32;
        
        private final int recordsCount;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedResult implements SingleTableInventoryCalculatedResult {
        
        private final int recordsCount;
        
        private final Collection<Long> columnsCrc32;
        
        @Override
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (this == o) {
                return true;
            }
            if (getClass() != o.getClass()) {
                log.warn("RecordSingleTableInventoryCalculatedResult type not match, o.className={}", o.getClass().getName());
                return false;
            }
            final CalculatedResult that = (CalculatedResult) o;
            if (recordsCount != that.recordsCount) {
                log.info("recordsCount not match, recordsCount={}, that.recordsCount={}", recordsCount, that.recordsCount);
                return false;
            }
            if (!columnsCrc32.equals(that.columnsCrc32)) {
                log.info("columnsCrc32 not match, columnsCrc32={}, that.columnsCrc32={}", columnsCrc32, that.columnsCrc32);
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int result = recordsCount;
            result = 31 * result + columnsCrc32.hashCode();
            return result;
        }
        
        // TODO not support now
        @Override
        public Optional<Object> getMaxUniqueKeyValue() {
            return Optional.empty();
        }
    }
}
