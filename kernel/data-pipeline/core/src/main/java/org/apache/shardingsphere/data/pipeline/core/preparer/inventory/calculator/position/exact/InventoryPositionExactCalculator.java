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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Inventory position exact calculator.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public final class InventoryPositionExactCalculator {
    
    /**
     * Get positions by table unique key range.
     *
     * @param <T> type of unique key
     * @param qualifiedTable qualified table
     * @param uniqueKey unique key
     * @param shardingSize sharding size
     * @param dataSource data source
     * @param positionHandler position handler
     * @return positions
     * @throws SplitPipelineJobByUniqueKeyException if an error occurs while splitting table by unique key
     */
    public static <T> List<IngestPosition> getPositions(final QualifiedTable qualifiedTable, final String uniqueKey, final int shardingSize,
                                                        final PipelineDataSource dataSource, final DataTypePositionHandler<T> positionHandler) {
        List<IngestPosition> result = new LinkedList<>();
        UniqueKeyIngestPosition<T> firstPosition = getFirstPosition(qualifiedTable, uniqueKey, shardingSize, dataSource, positionHandler);
        result.add(firstPosition);
        result.addAll(getLeftPositions(qualifiedTable, uniqueKey, shardingSize, firstPosition, dataSource, positionHandler));
        return result;
    }
    
    private static <T> UniqueKeyIngestPosition<T> getFirstPosition(final QualifiedTable qualifiedTable, final String uniqueKey, final int shardingSize,
                                                                    final PipelineDataSource dataSource, final DataTypePositionHandler<T> positionHandler) {
        String firstQuerySQL = new PipelinePrepareSQLBuilder(dataSource.getDatabaseType())
                .buildSplitByUniqueKeyRangedSQL(qualifiedTable.getSchemaName(), qualifiedTable.getTableName(), uniqueKey, false);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(firstQuerySQL)) {
            preparedStatement.setLong(1, shardingSize);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    log.info("No any record, return. First query SQL: {}", firstQuerySQL);
                    return positionHandler.createIngestPosition(null, null);
                }
                long count = resultSet.getLong(2);
                T minValue = positionHandler.readColumnValue(resultSet, 3);
                T maxValue = positionHandler.readColumnValue(resultSet, 1);
                log.info("First records count: {}, min value: {}, max value: {}, sharding size: {}, first query SQL: {}", count, minValue, maxValue, shardingSize, firstQuerySQL);
                if (0 == count) {
                    return positionHandler.createIngestPosition(null, null);
                }
                return positionHandler.createIngestPosition(minValue, maxValue);
            }
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(qualifiedTable.getTableName(), uniqueKey, ex);
        }
    }
    
    private static <T> List<IngestPosition> getLeftPositions(final QualifiedTable qualifiedTable, final String uniqueKey,
                                                             final int shardingSize, final UniqueKeyIngestPosition<T> firstPosition,
                                                             final PipelineDataSource dataSource, final DataTypePositionHandler<T> positionHandler) {
        List<IngestPosition> result = new LinkedList<>();
        T lowerBound = firstPosition.getUpperBound();
        long recordsCount = 0;
        String laterQuerySQL = new PipelinePrepareSQLBuilder(dataSource.getDatabaseType())
                .buildSplitByUniqueKeyRangedSQL(qualifiedTable.getSchemaName(), qualifiedTable.getTableName(), uniqueKey, true);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(laterQuerySQL)) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                positionHandler.setPreparedStatementValue(preparedStatement, 1, lowerBound);
                preparedStatement.setLong(2, shardingSize);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        break;
                    }
                    int count = resultSet.getInt(2);
                    if (0 == count) {
                        log.info("Done. Later records count: {}, last lower bound: {}, sharding size: {}, later query SQL: {}", recordsCount, lowerBound, shardingSize, laterQuerySQL);
                        break;
                    }
                    recordsCount += count;
                    T minValue = positionHandler.readColumnValue(resultSet, 3);
                    T maxValue = positionHandler.readColumnValue(resultSet, 1);
                    result.add(positionHandler.createIngestPosition(minValue, maxValue));
                    lowerBound = maxValue;
                }
            }
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(qualifiedTable.getTableName(), uniqueKey, ex);
        }
        return result;
    }
}
