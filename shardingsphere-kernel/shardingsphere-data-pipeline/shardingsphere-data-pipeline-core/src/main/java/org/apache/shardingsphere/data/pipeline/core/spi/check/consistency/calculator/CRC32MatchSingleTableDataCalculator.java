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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency.calculator;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRC32 match single table data calculator.
 */
public final class CRC32MatchSingleTableDataCalculator implements SingleTableDataCalculator {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getName());
    
    @Override
    public void init() {
    }
    
    @Override
    public Iterable<Object> calculate(final DataCalculateParameter dataCalculateParameter) {
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.newInstance(dataCalculateParameter.getDatabaseType());
        return Collections.unmodifiableList(dataCalculateParameter.getColumnNames().stream().map(each -> calculateCRC32(sqlBuilder, dataCalculateParameter, each)).collect(Collectors.toList()));
    }
    
    private long calculateCRC32(final PipelineSQLBuilder sqlBuilder, final DataCalculateParameter dataCalculateParameter, final String columnName) {
        Optional<String> sql = sqlBuilder.buildCRC32SQL(dataCalculateParameter.getLogicTableName(), columnName);
        if (!sql.isPresent()) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("Unsupported CRC32MatchSingleTableDataCalculator with database type `%s`", dataCalculateParameter.getDatabaseType()));
        }
        try {
            return calculateCRC32(dataCalculateParameter.getDataSource(), sql.get());
        } catch (final SQLException ex) {
            throw new PipelineDataConsistencyCheckFailedException(String.format("Table `%s` data check failed.", dataCalculateParameter.getLogicTableName()), ex);
        }
    }
    
    private long calculateCRC32(final DataSource dataSource, final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
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
}
