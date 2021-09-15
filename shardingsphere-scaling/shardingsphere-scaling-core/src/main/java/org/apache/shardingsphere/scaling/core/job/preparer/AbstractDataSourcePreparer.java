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

package org.apache.shardingsphere.scaling.core.job.preparer;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.yaml.ShardingRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    protected DataSourceWrapper getSourceDataSource(final JobConfiguration jobConfig) {
        return dataSourceFactory.newInstance(jobConfig.getRuleConfig().getSource().unwrap());
    }
    
    protected DataSourceWrapper getTargetDataSource(final JobConfiguration jobConfig) {
        return dataSourceFactory.newInstance(jobConfig.getRuleConfig().getTarget().unwrap());
    }
    
    protected List<String> getLogicTableNames(final ScalingDataSourceConfiguration sourceConfig) {
        List<String> result = new ArrayList<>();
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        ShardingRuleConfiguration ruleConfig = ShardingRuleConfigurationSwapper.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        List<String> tableNames = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        List<String> autoTableNames = ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(tableNames);
        result.addAll(autoTableNames);
        return result;
    }
    
    protected void createTargetTable(final Connection targetConnection, final String createTableSQL) throws SQLException {
        String sql = addIfNotExistsForCreateTableSQL(createTableSQL);
        log.info("create target table, sql: {}", sql);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
}
