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
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.governance.core.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceRuleConfigurationWrap;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * ShardingSphere-JDBC data source configuration.
 */
@Getter
@EqualsAndHashCode(of = "parameter")
public final class ShardingSphereJDBCDataSourceConfiguration implements ScalingDataSourceConfiguration {
    
    /**
     * Config type.
     */
    public static final String CONFIG_TYPE = "ShardingSphereJDBC";
    
    private final String parameter;
    
    private final YamlDataSourceRuleConfigurationWrap dataSourceRuleConfig;
    
    private final DatabaseType databaseType;
    
    public ShardingSphereJDBCDataSourceConfiguration(final String parameter) {
        this.parameter = parameter;
        dataSourceRuleConfig = YamlEngine.unmarshal(parameter, YamlDataSourceRuleConfigurationWrap.class);
        Map<String, Object> props = dataSourceRuleConfig.getDataSources().values().iterator().next().getProps();
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(props.getOrDefault("url", props.get("jdbcUrl")).toString());
    }
    
    public ShardingSphereJDBCDataSourceConfiguration(final String dataSources, final String rules) {
        this(String.format("%s\n%s", dataSources, rules));
    }
    
    @Override
    public String getConfigType() {
        return CONFIG_TYPE;
    }
    
    @Override
    public ScalingDataSourceConfigurationWrap wrap() {
        ScalingDataSourceConfigurationWrap result = new ScalingDataSourceConfigurationWrap();
        result.setType(CONFIG_TYPE);
        result.setParameter(parameter);
        return result;
    }
    
    @Override
    public DataSource toDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(YamlConfigurationConverter.convertDataSources(dataSourceRuleConfig.getDataSources()),
                Lists.newArrayList(YamlConfigurationConverter.convertShardingRuleConfig(dataSourceRuleConfig.getRules())), null);
    }
}
