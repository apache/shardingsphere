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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource;

import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineCommonSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithInvalidConnectionException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithTargetTableNotEmptyException;
import org.apache.shardingsphere.data.pipeline.spi.check.DialectDataSourceChecker;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Data source check engine.
 */
public final class DataSourceCheckEngine {
    
    private final DatabaseType databaseType;
    
    private final DialectDataSourceChecker dialectDataSourceChecker;
    
    public DataSourceCheckEngine(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        dialectDataSourceChecker = DatabaseTypedSPILoader.findService(DialectDataSourceChecker.class, databaseType).orElse(null);
    }
    
    /**
     * Check data source connections.
     *
     * @param dataSources data sources
     * @throws PrepareJobWithInvalidConnectionException prepare job with invalid connection exception
     */
    public void checkConnection(final Collection<? extends DataSource> dataSources) {
        try {
            for (DataSource each : dataSources) {
                each.getConnection().close();
            }
        } catch (final SQLException ex) {
            throw new PrepareJobWithInvalidConnectionException(ex);
        }
    }
    
    /**
     * Check table is empty.
     *
     * @param dataSources data sources
     * @param tableNameSchemaNameMapping mapping
     * @param logicTableNames logic table names
     * @throws PrepareJobWithInvalidConnectionException prepare job with invalid connection exception
     */
    // TODO rename to common usage name
    // TODO Merge schemaName and tableNames
    public void checkTargetTable(final Collection<? extends DataSource> dataSources, final TableNameSchemaNameMapping tableNameSchemaNameMapping, final Collection<String> logicTableNames) {
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
        PipelineCommonSQLBuilder pipelineSQLBuilder = new PipelineCommonSQLBuilder(databaseType);
        String sql = pipelineSQLBuilder.buildCheckEmptySQL(schemaName, tableName);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return !resultSet.next();
        }
    }
    
    /**
     * Check user privileges.
     *
     * @param dataSources data sources
     */
    public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
        if (null == dialectDataSourceChecker) {
            return;
        }
        for (DataSource each : dataSources) {
            dialectDataSourceChecker.checkPrivilege(each);
        }
    }
    
    /**
     * Check data source variables.
     *
     * @param dataSources data sources
     */
    public void checkVariable(final Collection<? extends DataSource> dataSources) {
        if (null == dialectDataSourceChecker) {
            return;
        }
        for (DataSource each : dataSources) {
            dialectDataSourceChecker.checkVariable(each);
        }
    }
}
