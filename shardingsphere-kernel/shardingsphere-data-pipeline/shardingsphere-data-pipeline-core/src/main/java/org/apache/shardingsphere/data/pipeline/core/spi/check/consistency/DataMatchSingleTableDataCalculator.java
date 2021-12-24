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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.DataCheckFailException;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.scaling.core.job.sqlbuilder.ScalingSQLBuilderFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Data match implementation of single table data calculator.
 */
public final class DataMatchSingleTableDataCalculator extends AbstractSingleTableDataCalculator {
    
    private static final Collection<String> DATABASE_TYPES = DatabaseTypeRegistry.getDatabaseTypeNames();
    
    @Override
    public String getAlgorithmType() {
        return DataMatchDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public Collection<String> getDatabaseTypes() {
        return DATABASE_TYPES;
    }
    
    @Override
    protected Optional<Object> calculateChunk(final DataCalculateParameter dataCalculateParameter) {
        String logicTableName = dataCalculateParameter.getLogicTableName();
        PipelineSQLBuilder sqlBuilder = ScalingSQLBuilderFactory.newInstance(dataCalculateParameter.getDatabaseType());
        String uniqueKey = dataCalculateParameter.getUniqueKey();
        Integer chunkSize = dataCalculateParameter.getChunkSize();
        if (null == chunkSize) {
            chunkSize = 1000;
        }
        CalculatedResult previousCalculatedResult = (CalculatedResult) dataCalculateParameter.getPreviousCalculatedResult();
        Number startUniqueValue = (null != previousCalculatedResult ? previousCalculatedResult.getMaxUniqueValue() : 0).longValue() - 1;
        String sql = sqlBuilder.buildChunkedQuerySQL(logicTableName, uniqueKey, startUniqueValue);
        try {
            return query(dataCalculateParameter.getDataSource(), sql, uniqueKey, startUniqueValue, chunkSize);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s data check failed.", logicTableName), ex);
        }
    }
    
    private Optional<Object> query(final DataSource dataSource, final String sql, final String uniqueKey, final Number startUniqueValue, final int chunkSize) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, startUniqueValue);
            preparedStatement.setInt(2, chunkSize);
            Collection<Collection<Object>> records = new ArrayList<>(chunkSize);
            Number maxUniqueValue = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    Collection<Object> record = new ArrayList<>(columnCount);
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        record.add(resultSet.getObject(columnIndex));
                    }
                    records.add(record);
                    maxUniqueValue = (Number) resultSet.getObject(uniqueKey);
                }
            }
            return records.isEmpty() ? Optional.empty() : Optional.of(new CalculatedResult(maxUniqueValue, records.size(), records));
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    private static final class CalculatedResult {
        
        private final Number maxUniqueValue;
        
        private final int recordCount;
        
        private final Collection<Collection<Object>> records;
    }
}
