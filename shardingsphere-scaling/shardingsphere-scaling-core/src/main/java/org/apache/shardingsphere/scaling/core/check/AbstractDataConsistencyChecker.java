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

package org.apache.shardingsphere.scaling.core.check;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.AbstractSqlBuilder;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.utils.ConfigurationYamlConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Abstract data consistency checker.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractDataConsistencyChecker implements DataConsistencyChecker {
    
    private final ShardingScalingJob shardingScalingJob;
    
    @Override
    public boolean countCheck() {
        return shardingScalingJob.getSyncConfigurations().stream().allMatch(each -> each.getDumperConfiguration().getTableNameMap().values().stream().distinct().allMatch(this::countCheck));
    }
    
    private boolean countCheck(final String table) {
        long sourceCount = count(getSourceDataSource(), table);
        long destinationCount = count(getDestinationDataSource(), table);
        if (sourceCount == destinationCount) {
            return true;
        }
        throw new DataCheckFailException(String.format("table %s count %d -> %d, check failed.", table, sourceCount, destinationCount));
    }
    
    protected DataSource getSourceDataSource() {
        try {
            Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(
                    ConfigurationYamlConverter.loadDataSourceConfigurations(shardingScalingJob.getScalingConfiguration().getRuleConfiguration().getSourceDatasource()));
            ShardingRuleConfiguration ruleConfiguration = ConfigurationYamlConverter.loadShardingRuleConfiguration(shardingScalingJob.getScalingConfiguration().getRuleConfiguration().getSourceRule());
            return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Lists.newArrayList(ruleConfiguration), null);
        } catch (SQLException ex) {
            throw new DataCheckFailException("get source data source failed.", ex);
        }
    }
    
    protected DataSource getDestinationDataSource() {
        RuleConfiguration.YamlDataSourceParameter parameter = shardingScalingJob.getScalingConfiguration().getRuleConfiguration().getDestinationDataSources();
        return new DataSourceFactory().newInstance(new JDBCDataSourceConfiguration(parameter.getUrl(), parameter.getUsername(), parameter.getPassword()));
    }
    
    private long count(final DataSource dataSource, final String table) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getSqlBuilder().buildCountSQL(table));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new DataCheckFailException(String.format("count %s failed.", table), ex);
        }
    }
    
    protected abstract AbstractSqlBuilder getSqlBuilder();
}
