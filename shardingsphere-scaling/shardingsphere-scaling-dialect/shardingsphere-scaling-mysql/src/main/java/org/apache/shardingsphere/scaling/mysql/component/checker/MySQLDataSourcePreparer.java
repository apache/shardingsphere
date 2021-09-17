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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.AbstractDataSourcePreparer;
import org.apache.shardingsphere.scaling.mysql.component.MySQLScalingSQLBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Data source preparer for MySQL.
 */
@Slf4j
public final class MySQLDataSourcePreparer extends AbstractDataSourcePreparer {
    
    private final MySQLScalingSQLBuilder scalingSQLBuilder = new MySQLScalingSQLBuilder(Collections.emptyMap());
    
    @Override
    public void prepareTargetTables(final JobConfiguration jobConfig) {
        try (DataSourceWrapper sourceDataSource = getSourceDataSource(jobConfig);
             Connection sourceConnection = sourceDataSource.getConnection();
             DataSourceWrapper targetDataSource = getTargetDataSource(jobConfig);
             Connection targetConnection = targetDataSource.getConnection()) {
            List<String> logicTableNames = getLogicTableNames(jobConfig.getRuleConfig().getSource().unwrap());
            for (String each : logicTableNames) {
                String createTableSQL = getCreateTableSQL(sourceConnection, each);
                createTargetTable(targetConnection, createTableSQL);
                log.info("create target table '{}' success", each);
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("prepare target tables failed.", ex);
        }
    }
    
    private String getCreateTableSQL(final Connection sourceConnection, final String logicTableName) throws SQLException {
        String showCreateTableSQL = "SHOW CREATE TABLE " + scalingSQLBuilder.quote(logicTableName);
        try (Statement statement = sourceConnection.createStatement(); ResultSet resultSet = statement.executeQuery(showCreateTableSQL)) {
            if (!resultSet.next()) {
                throw new PrepareFailedException("show create table has no result, sql: " + showCreateTableSQL);
            }
            return resultSet.getString(2);
        }
    }
}
