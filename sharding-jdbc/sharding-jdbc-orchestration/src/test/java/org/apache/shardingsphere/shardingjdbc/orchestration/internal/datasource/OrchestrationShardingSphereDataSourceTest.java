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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.masterslave.api.config.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationSchema;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationShardingSphereDataSourceTest {
    
    private static OrchestrationShardingSphereDataSource orchestrationDataSource;
    
    @BeforeClass
    public static void setUp() throws SQLException, IOException, URISyntaxException {
        orchestrationDataSource = new OrchestrationShardingSphereDataSource(getShardingSphereDataSource(), getOrchestrationConfiguration());
    }
    
    private static ShardingSphereDataSource getShardingSphereDataSource() throws IOException, SQLException, URISyntaxException {
        File yamlFile = new File(OrchestrationShardingSphereDataSourceTest.class.getResource("/yaml/unit/sharding.yaml").toURI());
        return (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
    }
    
    private static OrchestrationConfiguration getOrchestrationConfiguration() {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>();
        instanceConfigurationMap.put("test_sharding_registry_name", getRegistryCenterConfiguration());
        instanceConfigurationMap.put("test_sharding_config_name", getConfigCenterConfiguration());
        instanceConfigurationMap.put("test_sharding_metadata_name", getMetaDataCenterConfiguration());
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
    
    private static CenterConfiguration getRegistryCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FourthTestRegistryCenter");
        result.setOrchestrationType(CenterType.REGISTRY_CENTER.getValue());
        result.setNamespace("test_sharding_registry");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    private static CenterConfiguration getConfigCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FourthTestConfigCenter");
        result.setOrchestrationType(CenterType.CONFIG_CENTER.getValue());
        result.setNamespace("test_sharding_config");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    private static CenterConfiguration getMetaDataCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FirstTestMetaDataCenter");
        result.setOrchestrationType(CenterType.METADATA_CENTER.getValue());
        result.setNamespace("test_encrypt_metadata");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    @Test
    public void assertInitializeOrchestrationShardingSphereDataSource() throws SQLException {
        OrchestrationShardingSphereDataSource orchestrationShardingSphereDataSource = new OrchestrationShardingSphereDataSource(getOrchestrationConfiguration());
        assertThat(orchestrationShardingSphereDataSource.getConnection(), instanceOf(Connection.class));
    }
    
    @Test
    public void assertRenewRules() {
        orchestrationDataSource.renew(new RuleConfigurationsChangedEvent(DefaultSchema.LOGIC_NAME, Arrays.asList(getShardingRuleConfiguration(), getMasterSlaveRuleConfiguration())));
        assertThat(((ShardingRule) orchestrationDataSource.getDataSource().getRuntimeContext().getRules().iterator().next()).getTableRules().size(), is(1));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTableRuleConfigs().add(new TableRuleConfiguration("logic_table", "ds_ms.table_${0..1}"));
        return result;
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceConfiguration dataSourceConfiguration = new MasterSlaveDataSourceConfiguration(
                "ds_ms", "ds_m", Collections.singletonList("ds_s"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN"));
        return new MasterSlaveRuleConfiguration(Collections.singleton(dataSourceConfiguration));
    }
    
    @Test
    public void assertRenewDataSource() {
        orchestrationDataSource.renew(new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, getDataSourceConfigurations()));
        assertThat(orchestrationDataSource.getDataSource().getDataSourceMap().size(), is(3));
        
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("ds_m", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_s", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_0", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertRenewProperties() {
        orchestrationDataSource.renew(getPropertiesChangedEvent());
        assertThat(orchestrationDataSource.getDataSource().getRuntimeContext().getProperties().getProps().getProperty("sql.show"), is("true"));
    }
    
    private PropertiesChangedEvent getPropertiesChangedEvent() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        return new PropertiesChangedEvent(properties);
    }
    
    @Test
    public void assertRenewDisabledState() {
        orchestrationDataSource.renew(new DisabledStateChangedEvent(new OrchestrationSchema("logic_db.ds_s"), true));
    }
}
