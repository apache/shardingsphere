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

package org.apache.shardingsphere.scaling.core.datasource;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ShardingSphereJDBCConfiguration;
import org.apache.shardingsphere.scaling.core.utils.ConfigurationYamlConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Data source factory.
 */
public final class DataSourceFactory {
    
    /**
     * New instance data source.
     *
     * @param dataSourceConfiguration data source configuration
     * @return new data source
     */
    public DataSourceWrapper newInstance(final DataSourceConfiguration dataSourceConfiguration) {
        if (dataSourceConfiguration instanceof JDBCDataSourceConfiguration) {
            return newInstanceDataSourceByJDBC((JDBCDataSourceConfiguration) dataSourceConfiguration);
        } else if (dataSourceConfiguration instanceof ShardingSphereJDBCConfiguration) {
            return newInstanceDataSourceByShardingSphereJDBC((ShardingSphereJDBCConfiguration) dataSourceConfiguration);
        }
        throw new UnsupportedOperationException("Unsupported data source configuration");
    }
    
    private DataSourceWrapper newInstanceDataSourceByJDBC(final JDBCDataSourceConfiguration dataSourceConfiguration) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(dataSourceConfiguration.getJdbcUrl());
        result.setUsername(dataSourceConfiguration.getUsername());
        result.setPassword(dataSourceConfiguration.getPassword());
        return new DataSourceWrapper(result);
    }
    
    @SneakyThrows
    private DataSourceWrapper newInstanceDataSourceByShardingSphereJDBC(final ShardingSphereJDBCConfiguration dataSourceConfiguration) {
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(
                ConfigurationYamlConverter.loadDataSourceConfigurations(dataSourceConfiguration.getDataSource()));
        ShardingRuleConfiguration ruleConfiguration = ConfigurationYamlConverter.loadShardingRuleConfiguration(dataSourceConfiguration.getRule());
        return new DataSourceWrapper(ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Lists.newArrayList(ruleConfiguration), null));
    }
}
