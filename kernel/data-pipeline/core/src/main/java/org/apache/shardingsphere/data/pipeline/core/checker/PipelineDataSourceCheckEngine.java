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

package org.apache.shardingsphere.data.pipeline.core.checker;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithTargetTableNotEmptyException;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Pipeline data source check engine.
 */
public final class PipelineDataSourceCheckEngine {
    
    private final DatabaseType databaseType;
    
    private final PipelinePrepareSQLBuilder sqlBuilder;
    
    public PipelineDataSourceCheckEngine(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        sqlBuilder = new PipelinePrepareSQLBuilder(databaseType);
    }
    
    /**
     * Check data source connections.
     *
     * @param dataSources data sources
     * @throws SQLWrapperException SQL wrapper exception
     */
    public void checkConnection(final Collection<DataSource> dataSources) {
        try {
            for (DataSource each : dataSources) {
                each.getConnection().close();
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    /**
     * Check source data source.
     *
     * @param dataSources to be checked source data source
     */
    public void checkSourceDataSources(final Collection<DataSource> dataSources) {
        checkConnection(dataSources);
        DatabaseTypedSPILoader.findService(DialectDatabasePrivilegeChecker.class, databaseType).ifPresent(optional -> dataSources.forEach(each -> optional.check(each, PrivilegeCheckType.PIPELINE)));
        DatabaseTypedSPILoader.findService(DialectPipelineDatabaseVariableChecker.class, databaseType).ifPresent(optional -> dataSources.forEach(optional::check));
    }
    
    /**
     * Check target data sources.
     *
     * @param dataSources to be checked target data sources
     * @param importerConfig importer configuration
     */
    public void checkTargetDataSources(final Collection<DataSource> dataSources, final ImporterConfiguration importerConfig) {
        checkConnection(dataSources);
        checkEmptyTable(dataSources, importerConfig);
    }
    
    private void checkEmptyTable(final Collection<DataSource> dataSources, final ImporterConfiguration importerConfig) {
        try {
            for (DataSource each : dataSources) {
                for (QualifiedTable qualifiedTable : importerConfig.getTableAndSchemaNameMapper().getQualifiedTables()) {
                    ShardingSpherePreconditions.checkState(checkEmptyTable(each, qualifiedTable), () -> new PrepareJobWithTargetTableNotEmptyException(qualifiedTable.getTableName()));
                }
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    /**
     * Check whether empty table.
     *
     * @param dataSource data source
     * @param qualifiedTable qualified table
     * @return empty or not
     * @throws SQLException if there's database operation failure
     */
    public boolean checkEmptyTable(final DataSource dataSource, final QualifiedTable qualifiedTable) throws SQLException {
        String sql = sqlBuilder.buildCheckEmptyTableSQL(qualifiedTable.getSchemaName(), qualifiedTable.getTableName());
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return !resultSet.next();
        }
    }
}
