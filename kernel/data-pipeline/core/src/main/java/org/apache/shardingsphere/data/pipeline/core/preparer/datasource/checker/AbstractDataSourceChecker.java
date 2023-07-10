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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource.checker;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithInvalidConnectionException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithTargetTableNotEmptyException;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Abstract data source checker.
 */
@Slf4j
public abstract class AbstractDataSourceChecker implements DataSourceChecker {
    
    @Override
    public final void checkConnection(final Collection<? extends DataSource> dataSources) {
        try {
            for (DataSource each : dataSources) {
                each.getConnection().close();
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithInvalidConnectionException(ex);
        }
    }
    
    @Override
    public final void checkTargetTable(final Collection<? extends DataSource> dataSources, final TableNameSchemaNameMapping tableNameSchemaNameMapping, final Collection<String> logicTableNames) {
        try {
            for (DataSource each : dataSources) {
                for (String tableName : logicTableNames) {
                    if (!checkEmpty(each, tableNameSchemaNameMapping.getSchemaName(tableName), tableName)) {
                        throw new PrepareJobWithTargetTableNotEmptyException(tableName);
                    }
                }
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithInvalidConnectionException(ex);
        }
    }
    
    private boolean checkEmpty(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, getDatabaseType());
        String sql = DatabaseTypedSPILoader.getService(PipelineSQLBuilder.class, databaseType).buildCheckEmptySQL(schemaName, tableName);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return !resultSet.next();
        }
    }
}
