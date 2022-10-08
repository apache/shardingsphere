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
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedCRC32DataConsistencyCalculateAlgorithmException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
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
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * CRC32 match data consistency calculate algorithm.
 */
@Getter
public final class CRC32MatchDataConsistencyCalculateAlgorithm implements DataConsistencyCalculateAlgorithm {
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getType());
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Iterable<Object> calculate(final DataConsistencyCalculateParameter parameter) {
        PipelineSQLBuilder sqlBuilder = PipelineSQLBuilderFactory.getInstance(parameter.getDatabaseType());
        return Collections.unmodifiableList(parameter.getColumnNames().stream().map(each -> calculateCRC32(sqlBuilder, parameter, each)).collect(Collectors.toList()));
    }
    
    private long calculateCRC32(final PipelineSQLBuilder sqlBuilder, final DataConsistencyCalculateParameter parameter, final String columnName) {
        String logicTableName = parameter.getLogicTableName();
        String schemaName = parameter.getSchemaName();
        Optional<String> sql = sqlBuilder.buildCRC32SQL(schemaName, logicTableName, columnName);
        ShardingSpherePreconditions.checkState(sql.isPresent(), () -> new UnsupportedCRC32DataConsistencyCalculateAlgorithmException(parameter.getDatabaseType()));
        return calculateCRC32(parameter.getDataSource(), logicTableName, sql.get());
    }
    
    private long calculateCRC32(final DataSource dataSource, final String logicTableName, final String sql) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
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
}
