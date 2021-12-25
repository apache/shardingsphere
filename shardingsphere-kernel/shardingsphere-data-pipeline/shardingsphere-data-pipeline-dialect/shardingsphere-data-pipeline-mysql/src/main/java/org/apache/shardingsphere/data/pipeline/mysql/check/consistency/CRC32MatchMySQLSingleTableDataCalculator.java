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

package org.apache.shardingsphere.data.pipeline.mysql.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.DataCheckFailException;
import org.apache.shardingsphere.data.pipeline.core.spi.check.consistency.AbstractSingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.core.spi.check.consistency.CRC32MatchDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder.MySQLPipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRC32 match MySQL implementation of single table data calculator.
 */
public final class CRC32MatchMySQLSingleTableDataCalculator extends AbstractSingleTableDataCalculator {
    
    private static final Collection<String> DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getName());
    
    @Override
    public String getAlgorithmType() {
        return CRC32MatchDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public Collection<String> getDatabaseTypes() {
        return DATABASE_TYPES;
    }
    
    @Override
    public Iterable<Object> calculate(final DataCalculateParameter dataCalculateParameter) {
        String logicTableName = dataCalculateParameter.getLogicTableName();
        MySQLPipelineSQLBuilder scalingSQLBuilder = new MySQLPipelineSQLBuilder(new HashMap<>());
        List<Long> result = dataCalculateParameter.getColumnNames().stream().map(each -> {
            String sql = scalingSQLBuilder.buildSumCrc32SQL(logicTableName, each);
            try {
                return sumCrc32(dataCalculateParameter.getDataSource(), sql);
            } catch (final SQLException ex) {
                throw new DataCheckFailException(String.format("table %s data check failed.", logicTableName), ex);
            }
        }).collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }
    
    private long sumCrc32(final DataSource dataSource, final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
