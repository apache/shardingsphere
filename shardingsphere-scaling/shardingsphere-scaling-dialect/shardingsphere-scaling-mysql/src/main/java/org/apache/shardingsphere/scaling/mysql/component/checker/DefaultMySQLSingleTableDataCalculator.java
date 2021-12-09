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

package org.apache.shardingsphere.scaling.mysql.component.checker;

import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.scaling.core.api.DataCalculateParameter;
import org.apache.shardingsphere.scaling.core.api.impl.AbstractSingleTableDataCalculator;
import org.apache.shardingsphere.scaling.core.api.impl.ScalingDefaultDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.mysql.component.MySQLScalingSQLBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Default MySQL single table data calculator.
 */
public final class DefaultMySQLSingleTableDataCalculator extends AbstractSingleTableDataCalculator {
    
    private static final String DATABASE_TYPE = new MySQLDatabaseType().getName();
    
    @Override
    public String getAlgorithmType() {
        return ScalingDefaultDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public String getDatabaseType() {
        return DATABASE_TYPE;
    }
    
    @Override
    public Object dataCalculate(final DataCalculateParameter dataCalculateParameter) {
        String logicTableName = dataCalculateParameter.getLogicTableName();
        MySQLScalingSQLBuilder scalingSQLBuilder = new MySQLScalingSQLBuilder(new HashMap<>());
        try (DataSourceWrapper dataSource = getDataSource(dataCalculateParameter.getDataSourceConfig())) {
            return dataCalculateParameter.getColumnNames().stream().map(each -> {
                String sql = scalingSQLBuilder.buildSumCrc32SQL(logicTableName, each);
                return sumCrc32(dataSource, sql);
            }).collect(Collectors.toList());
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s data check failed.", logicTableName), ex);
        }
    }
    
    private long sumCrc32(final DataSource dataSource, final String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("execute %s failed.", sql), ex);
        }
    }
}
