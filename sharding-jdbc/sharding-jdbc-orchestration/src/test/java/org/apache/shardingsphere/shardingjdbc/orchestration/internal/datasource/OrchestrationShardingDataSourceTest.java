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

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrchestrationShardingDataSourceTest {
    
    private static OrchestrationShardingDataSource shardingDataSource;
    
    @BeforeClass
    public static void setUp() throws SQLException, IOException, URISyntaxException {
        shardingDataSource = new OrchestrationShardingDataSource(getShardingDataSource(), getOrchestrationConfiguration());
    }
    
    private static ShardingDataSource getShardingDataSource() throws IOException, SQLException, URISyntaxException {
        File yamlFile = new File(OrchestrationShardingDataSourceTest.class.getResource("/yaml/unit/sharding.yaml").toURI());
        return (ShardingDataSource) YamlShardingDataSourceFactory.createDataSource(yamlFile);
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
    @SneakyThrows
    public void assertInitializeOrchestrationShardingDataSource() {
        OrchestrationShardingDataSource shardingDataSource = new OrchestrationShardingDataSource(getOrchestrationConfiguration());
        assertThat(shardingDataSource.getConnection(), instanceOf(Connection.class));
    }
    
    @Test
    public void assertRenewRule() {
        shardingDataSource.renew(new ShardingRuleChangedEvent(DefaultSchema.LOGIC_NAME, getShardingRuleConfig()));
        assertThat(((ShardingRule) shardingDataSource.getDataSource().getRuntimeContext().getRules().iterator().next()).getTableRules().size(), is(1));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTableRuleConfigs().add(new TableRuleConfiguration("logic_table", "ds_ms.table_${0..1}"));
        result.getMasterSlaveRuleConfigs().add(getMasterSlaveRuleConfiguration());
        return result;
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration("ds_ms", "ds_m", Collections.singletonList("ds_s"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN"));
    }
    
    @Test
    public void assertRenewDataSource() {
        shardingDataSource.renew(new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, getDataSourceConfigurations()));
        assertThat(shardingDataSource.getDataSource().getDataSourceMap().size(), is(3));
        
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
        shardingDataSource.renew(getPropertiesChangedEvent());
        assertThat(shardingDataSource.getDataSource().getRuntimeContext().getProperties().getProps().getProperty("sql.show"), is("true"));
    }
    
    private PropertiesChangedEvent getPropertiesChangedEvent() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        return new PropertiesChangedEvent(properties);
    }
    
    @Test
    public void assertRenewDisabledState() {
        shardingDataSource.renew(getDisabledStateChangedEvent());
        Optional<MasterSlaveRule> masterSlaveRule = shardingDataSource.getDataSource().getRuntimeContext().getRules().stream().filter(
            each -> each instanceof MasterSlaveRule).findFirst().map(rule -> (MasterSlaveRule) rule);
        assertTrue(masterSlaveRule.isPresent());
        assertThat(masterSlaveRule.get().getSlaveDataSourceNames().size(), is(0));
    }
    
    private DisabledStateChangedEvent getDisabledStateChangedEvent() {
        OrchestrationShardingSchema shardingSchema = new OrchestrationShardingSchema("logic_db.ds_s");
        return new DisabledStateChangedEvent(shardingSchema, true);
    }
}
