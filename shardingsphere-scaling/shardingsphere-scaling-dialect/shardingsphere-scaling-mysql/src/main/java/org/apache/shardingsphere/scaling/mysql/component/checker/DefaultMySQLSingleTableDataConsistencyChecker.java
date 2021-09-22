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

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.scaling.core.api.impl.AbstractSingleTableDataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.api.impl.ScalingDefaultDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.mysql.component.MySQLScalingSQLBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Default MySQL single table data consistency checker.
 */
public final class DefaultMySQLSingleTableDataConsistencyChecker extends AbstractSingleTableDataConsistencyChecker {
    
    private static final String DATABASE_TYPE = new MySQLDatabaseType().getName();
    
    private final MySQLScalingSQLBuilder scalingSQLBuilder = new MySQLScalingSQLBuilder(new HashMap<>());
    
    public DefaultMySQLSingleTableDataConsistencyChecker(final JobContext jobContext) {
        super(jobContext);
    }
    
    @Override
    public String getAlgorithmType() {
        return ScalingDefaultDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public String getDatabaseType() {
        return DATABASE_TYPE;
    }
    
    @Override
    public boolean dataCheck(final String logicTableName, final Collection<String> columnNames) {
        try (DataSourceWrapper sourceDataSource = getSourceDataSource();
             DataSourceWrapper targetDataSource = getTargetDataSource()) {
            return columnNames.stream().allMatch(each -> sumCrc32(sourceDataSource, logicTableName, each) == sumCrc32(targetDataSource, logicTableName, each));
        } catch (final SQLException ex) {
            throw new DataCheckFailException(String.format("table %s data check failed.", logicTableName), ex);
        }
    }
    
    private long sumCrc32(final DataSource dataSource, final String tableName, final String column) {
        String sql = scalingSQLBuilder.buildSumCrc32SQL(tableName, column);
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
