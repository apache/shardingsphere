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

package org.apache.shardingsphere.scaling.core.config.datasource;

import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * ShardingSphere-JDBC data source configuration.
 */
@Setter
@Getter
@EqualsAndHashCode(exclude = "databaseType")
public final class ShardingSphereJDBCDataSourceConfiguration implements ScalingDataSourceConfiguration {
    
    /**
     * Config type.
     */
    public static final String CONFIG_TYPE = "ShardingSphereJDBC";
    
    private String dataSource;
    
    private String rule;
    
    private transient DatabaseType databaseType;
    
    public ShardingSphereJDBCDataSourceConfiguration(final String dataSource, final String rule) {
        this.dataSource = dataSource;
        this.rule = rule;
    }
    
    @Override
    public String getConfigType() {
        return CONFIG_TYPE;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        if (null == databaseType) {
            Map<String, Object> props = ConfigurationYamlConverter.loadDataSourceConfigs(dataSource).values().iterator().next().getProps();
            databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(props.getOrDefault("url", props.get("jdbcUrl")).toString());
        }
        return databaseType;
    }
    
    @Override
    public DataSource toDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(ConfigurationYamlConverter.loadDataSourceConfigs(dataSource));
        ShardingRuleConfiguration ruleConfig = ConfigurationYamlConverter.loadShardingRuleConfig(rule);
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Lists.newArrayList(ruleConfig), null);
    }
}
