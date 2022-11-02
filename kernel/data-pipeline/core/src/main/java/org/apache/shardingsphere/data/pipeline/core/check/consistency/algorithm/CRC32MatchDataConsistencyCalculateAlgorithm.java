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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedCRC32DataConsistencyCalculateAlgorithmException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * CRC32 match data consistency calculate algorithm.
 */
@Slf4j
public final class CRC32MatchDataConsistencyCalculateAlgorithm extends AbstractDataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getType());
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Iterable<DataConsistencyCalculatedResult> calculate(final DataConsistencyCalculateParameter parameter) {
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.getInstance(parameter.getDatabaseType());
        List<CalculatedItem> calculatedItems = parameter.getColumnNames().stream().map(each -> calculateCRC32(sqlBuilder, parameter, each)).collect(Collectors.toList());
        return Collections.singletonList(new CalculatedResult(calculatedItems.get(0).getRecordsCount(), calculatedItems.stream().map(CalculatedItem::getCrc32).collect(Collectors.toList())));
    }
    
    private CalculatedItem calculateCRC32(final PipelineSQLBuilder sqlBuilder, final DataConsistencyCalculateParameter parameter, final String columnName) {
        String logicTableName = parameter.getLogicTableName();
        String schemaName = parameter.getSchemaName();
        Optional<String> sql = sqlBuilder.buildCRC32SQL(schemaName, logicTableName, columnName);
        ShardingSpherePreconditions.checkState(sql.isPresent(), () -> new UnsupportedCRC32DataConsistencyCalculateAlgorithmException(parameter.getDatabaseType()));
        return calculateCRC32(parameter.getDataSource(), logicTableName, sql.get());
    }
    
    private CalculatedItem calculateCRC32(final DataSource dataSource, final String logicTableName, final String sql) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = setCurrentStatement(connection.prepareStatement(sql));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            long crc32 = resultSet.getLong(1);
            int recordsCount = resultSet.getInt(2);
            return new CalculatedItem(crc32, recordsCount);
        } catch (final SQLException ex) {
            throw new PipelineTableDataConsistencyCheckLoadingFailedException(logicTableName);
        }
    }
    
    @Override
    public String getType() {
        return "CRC32_MATCH";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @Override
    public String getDescription() {
        return "Match CRC32 of records.";
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedItem {
        
        private final long crc32;
        
        private final int recordsCount;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class CalculatedResult implements DataConsistencyCalculatedResult {
        
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
                log.warn("CalculatedResult type not match, o.className={}", o.getClass().getName());
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
    }
}
