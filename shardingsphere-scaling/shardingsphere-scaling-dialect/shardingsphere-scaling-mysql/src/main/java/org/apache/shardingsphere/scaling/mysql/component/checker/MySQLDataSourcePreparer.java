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
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.yaml.ShardingRuleConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.job.preparer.DataSourcePreparer;
import org.apache.shardingsphere.scaling.mysql.component.MySQLScalingSQLBuilder;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data source preparer for MySQL.
 */
@Slf4j
public final class MySQLDataSourcePreparer implements DataSourcePreparer {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private final MySQLScalingSQLBuilder scalingSQLBuilder = new MySQLScalingSQLBuilder(Collections.emptyMap());
    
    @Override
    public void prepareTargetTables(final JobConfiguration jobConfig) {
        ScalingDataSourceConfiguration sourceConfig = jobConfig.getRuleConfig().getSource().unwrap();
        ScalingDataSourceConfiguration targetConfig = jobConfig.getRuleConfig().getTarget().unwrap();
        try (DataSourceWrapper sourceDataSource = dataSourceFactory.newInstance(sourceConfig);
             Connection sourceConnection = sourceDataSource.getConnection();
             DataSourceWrapper targetDataSource = dataSourceFactory.newInstance(targetConfig);
             Connection targetConnection = targetDataSource.getConnection()) {
            List<String> logicTableNames = getLogicTableNames(sourceConfig);
            for (String logicTableName : logicTableNames) {
                createTargetTable(sourceConnection, targetConnection, logicTableName);
                log.info("create target table '{}' success", logicTableName);
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("prepare target tables failed.", ex);
        }
    }
    
    private List<String> getLogicTableNames(final ScalingDataSourceConfiguration sourceConfig) {
        List<String> result = new ArrayList<>();
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        ShardingRuleConfiguration ruleConfig = ShardingRuleConfigurationSwapper.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        List<String> tableNames = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        List<String> autoTableNames = ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(tableNames);
        result.addAll(autoTableNames);
        return result;
    }
    
    private void createTargetTable(final Connection sourceConnection, final Connection targetConnection, final String logicTableName) throws SQLException {
        String createTableSQL = getCreateTableSQL(sourceConnection, logicTableName);
        log.info("logicTableName: {}, createTableSQL: {}", logicTableName, createTableSQL);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(createTableSQL);
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
