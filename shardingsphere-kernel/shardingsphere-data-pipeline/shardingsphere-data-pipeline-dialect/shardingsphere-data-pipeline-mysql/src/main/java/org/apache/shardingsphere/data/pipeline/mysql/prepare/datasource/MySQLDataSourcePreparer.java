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

package org.apache.shardingsphere.data.pipeline.mysql.prepare.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.AbstractDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder.MySQLPipelineSQLBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Data source preparer for MySQL.
 */
@Slf4j
public final class MySQLDataSourcePreparer extends AbstractDataSourcePreparer {
    
    private final MySQLPipelineSQLBuilder scalingSQLBuilder = new MySQLPipelineSQLBuilder(Collections.emptyMap());
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter parameter) {
        PipelineConfiguration pipelineConfig = parameter.getPipelineConfiguration();
        try (PipelineDataSourceWrapper sourceDataSource = getSourceDataSource(pipelineConfig);
             Connection sourceConnection = sourceDataSource.getConnection();
             PipelineDataSourceWrapper targetDataSource = getTargetDataSource(pipelineConfig);
             Connection targetConnection = targetDataSource.getConnection()) {
            Collection<String> logicTableNames = parameter.getTablesFirstDataNodes().getEntries().stream().map(JobDataNodeEntry::getLogicTableName).collect(Collectors.toList());
            for (String each : logicTableNames) {
                String createTableSQL = getCreateTableSQL(sourceConnection, each);
                createTableSQL = addIfNotExistsForCreateTableSQL(createTableSQL);
                executeTargetTableSQL(targetConnection, createTableSQL);
                log.info("create target table '{}' success", each);
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException("prepare target tables failed.", ex);
        }
    }
    
    private String getCreateTableSQL(final Connection sourceConnection, final String logicTableName) throws SQLException {
        String showCreateTableSQL = "SHOW CREATE TABLE " + scalingSQLBuilder.quote(logicTableName);
        try (Statement statement = sourceConnection.createStatement(); ResultSet resultSet = statement.executeQuery(showCreateTableSQL)) {
            if (!resultSet.next()) {
                throw new PipelineJobPrepareFailedException("show create table has no result, sql: " + showCreateTableSQL);
            }
            return resultSet.getString(2);
        }
    }
}
