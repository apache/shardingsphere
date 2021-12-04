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

package org.apache.shardingsphere.migration.common.job.preparer;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.typed.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.ActualTableDefinition;
import org.apache.shardingsphere.scaling.core.job.preparer.DataSourcePreparer;
import org.apache.shardingsphere.scaling.core.job.preparer.TableDefinitionSQLType;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract data source preparer.
 */
@Slf4j
public abstract class AbstractDataSourcePreparer implements DataSourcePreparer {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PATTERN_ALTER_TABLE = Pattern.compile("ALTER\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE);
    
    private static final String[] IGNORE_EXCEPTION_MESSAGE = {"multiple primary keys for table", "already exists"};
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    protected DataSourceWrapper getSourceDataSource(final JobConfiguration jobConfig) {
        return dataSourceFactory.newInstance(jobConfig.getRuleConfig().getSource().unwrap());
    }
    
    protected DataSourceWrapper getTargetDataSource(final JobConfiguration jobConfig) {
        return dataSourceFactory.newInstance(jobConfig.getRuleConfig().getTarget().unwrap());
    }
    
    protected Collection<String> getLogicTableNames(final TypedDataSourceConfiguration sourceConfig) {
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        ShardingRuleConfiguration ruleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        return getLogicTableNames(ruleConfig);
    }
    
    private Collection<String> getLogicTableNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = new ArrayList<>();
        List<String> tableNames = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        List<String> autoTableNames = ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(tableNames);
        result.addAll(autoTableNames);
        return result;
    }
    
    /**
     * Get data source table names map.
     *
     * @param sourceConfig source data source configuration
     * @return data source table names map. map(data source, map(first actual table name of logic table, logic table name)).
     */
    protected Map<DataSource, Map<String, String>> getDataSourceTableNamesMap(final TypedDataSourceConfiguration sourceConfig) {
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        ShardingRuleConfiguration ruleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        Map<String, DataSourceConfiguration> dataSourceConfigs = new YamlDataSourceConfigurationSwapper().getDataSourceConfigurations(source.getRootConfig());
        ShardingRule shardingRule = new ShardingRule(ruleConfig, source.getRootConfig().getDataSources().keySet());
        Collection<String> logicTableNames = getLogicTableNames(ruleConfig);
        Map<String, Map<String, String>> dataSourceNameTableNamesMap = new HashMap<>();
        for (String each : logicTableNames) {
            DataNode dataNode = shardingRule.getDataNode(each);
            dataSourceNameTableNamesMap.computeIfAbsent(dataNode.getDataSourceName(), key -> new LinkedHashMap<>()).put(dataNode.getTableName(), each);
        }
        return dataSourceNameTableNamesMap.entrySet().stream().collect(
                Collectors.toMap(entry -> DataSourceConverter.getDataSource(dataSourceConfigs.get(entry.getKey())), Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    protected void executeTargetTableSQL(final Connection targetConnection, final String sql) throws SQLException {
        log.info("execute target table sql: {}", sql);
        try (Statement statement = targetConnection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ex) {
            for (String ignoreMessage: IGNORE_EXCEPTION_MESSAGE) {
                if (ex.getMessage().contains(ignoreMessage)) {
                    return;
                }
            }
            throw ex;
        }
    }
    
    protected Collection<String> splitTableDefinitionToSQLs(final ActualTableDefinition actualTableDefinition) {
        return Arrays.stream(actualTableDefinition.getTableDefinition().split(";")).collect(Collectors.toList());
    }
    
    //TODO simple lexer
    protected TableDefinitionSQLType getTableDefinitionSQLType(final String sql) {
        if (PATTERN_CREATE_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.CREATE_TABLE;
        }
        if (PATTERN_ALTER_TABLE.matcher(sql).find()) {
            return TableDefinitionSQLType.ALTER_TABLE;
        }
        return TableDefinitionSQLType.UNKNOWN;
    }
    
    protected String addIfNotExistsForCreateTableSQL(final String createTableSQL) {
        if (PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(createTableSQL).find()) {
            return createTableSQL;
        }
        return PATTERN_CREATE_TABLE.matcher(createTableSQL).replaceFirst("CREATE TABLE IF NOT EXISTS ");
    }
    
    protected String replaceActualTableNameToLogicTableName(final String createOrAlterTableSQL, final String actualTableName, final String logicTableName) {
        int start = createOrAlterTableSQL.indexOf(actualTableName);
        if (start <= 0) {
            return createOrAlterTableSQL;
        }
        int end = start + actualTableName.length();
        return new StringBuilder(createOrAlterTableSQL).replace(start, end, logicTableName).toString();
    }
}
