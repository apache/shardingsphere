/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.config.service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationServiceTest {
    
    private static final String DATA_SOURCE_YAML = 
            "ds_0: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_0\n" + "    username: root\n" + "    password: root\n"
            + "ds_1: !!io.shardingsphere.core.config.DataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_1\n" + "    username: root\n" + "    password: root\n";
    
    private static final String DATA_SOURCE_PARAMETER_YAML = "ds_0: !!io.shardingsphere.core.rule.DataSourceParameter\n"
            + "  url: jdbc:mysql://localhost:3306/ds_0\n" + "  username: root\n" + "  password: root\n"
            + "ds_1: !!io.shardingsphere.core.rule.DataSourceParameter\n"
            + "  url: jdbc:mysql://localhost:3306/ds_1\n" + "  username: root\n" + "  password: root\n";
    
    private static final String SHARDING_RULE_YAML = "tables:\n" + "  t_order:\n"
            + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "    logicTable: t_order\n" + "    "
            + "tableStrategy:\n" + "      inline:\n" + "        algorithmExpression: t_order_${order_id % 2}\n" + "        shardingColumn: order_id\n";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private static final String SHARDING_MASTER_SLAVE_RULE_YAML = "tables:\n" + "  t_order: \n" + "    actualDataNodes: db_ms_${0..1}.t_order_${0..1}\n" + "    databaseStrategy: \n"
            + "      inline:\n" + "        shardingColumn: user_id\n" + "        algorithmExpression: db_ms_${user_id % 2}\n" + "    tableStrategy: \n" + "      inline:\n"
            + "        shardingColumn: order_id\n" + "        algorithmExpression: t_order_${order_id % 2}\n" + "    keyGeneratorColumnName: order_id\n"
            + "bindingTables:\n" + "  - t_order,t_order_item\n" + "masterSlaveRules:\n" + "  db_ms_0:\n" + "    masterDataSourceName: db0_master\n" + "    slaveDataSourceNames:\n"
            + "      - db0_slave\n" + "  db_ms_1:\n" + "    masterDataSourceName: db1_master\n" + "    slaveDataSourceNames:\n" + "      - db1_slave";
    
    private static final String AUTHENTICATION_YAML = "password: root\n" + "username: root\n";
    
    private static final String CONFIG_MAP_YAML = "{}\n";
    
    private static final String PROPS_YAML = "sql.show: false\n";
    
    @Mock
    private RegistryCenter regCenter;
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(regCenter.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(regCenter.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(regCenter.get("/test/config/configmap")).thenReturn(CONFIG_MAP_YAML);
        when(regCenter.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter, times(0)).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), true);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(regCenter.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(regCenter.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(regCenter.get("/test/config/configmap")).thenReturn(CONFIG_MAP_YAML);
        when(regCenter.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter, times(0)).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db", 
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), null, Collections.<String, Object>emptyMap(), createProperties(), true);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(regCenter.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_PARAMETER_YAML);
        when(regCenter.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(regCenter.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        when(regCenter.get("/test/config/configmap")).thenReturn(CONFIG_MAP_YAML);
        when(regCenter.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter, times(0)).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter, times(0)).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createShardingRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), true);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(regCenter).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(regCenter.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_PARAMETER_YAML);
        when(regCenter.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(regCenter.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        when(regCenter.get("/test/config/configmap")).thenReturn(CONFIG_MAP_YAML);
        when(regCenter.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter, times(0)).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter, times(0)).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), false);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsOverwrite() {
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        configurationService.persistConfiguration("sharding_db",
                createDataSourceConfigurationMap(), createMasterSlaveRuleConfiguration(), createAuthentication(), Collections.<String, Object>emptyMap(), createProperties(), true);
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(regCenter).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(regCenter).persist("/test/config/configmap", CONFIG_MAP_YAML);
        verify(regCenter).persist("/test/config/props", PROPS_YAML);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurationMap() {
        return Maps.transformValues(createDataSourceMap(), new Function<DataSource, DataSourceConfiguration>() {
        
            @Override
            public DataSourceConfiguration apply(final DataSource input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
    
    private DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
        return DataSourceConfiguration.getDataSourceConfiguration(dataSource);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        return new Yaml().loadAs(SHARDING_RULE_YAML, YamlShardingRuleConfiguration.class).getShardingRuleConfiguration();
    }
    
    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() {
        return new Yaml().loadAs(MASTER_SLAVE_RULE_YAML, YamlMasterSlaveRuleConfiguration.class).getMasterSlaveRuleConfiguration();
    }
    
    private Authentication createAuthentication() {
        Authentication result = new Authentication();
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ShardingPropertiesConstant.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertLoadDataSources() {
        when(regCenter.getDirectly("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), createDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), createDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProperties().get("url"), is(expected.getProperties().get("url")));
        assertThat(actual.getProperties().get("username"), is(expected.getProperties().get("username")));
        assertThat(actual.getProperties().get("password"), is(expected.getProperties().get("password")));
    }
    
    @Test
    public void assertIsShardingRule() {
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        assertTrue(configurationService.isShardingRule("sharding_db"));
    }
    
    @Test
    public void assertIsNotShardingRule() {
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        assertFalse(configurationService.isShardingRule("sharding_db"));
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        ShardingRuleConfiguration actual = configurationService.loadShardingRuleConfiguration("sharding_db");
        assertThat(actual.getTableRuleConfigs().size(), is(1));
        assertThat(actual.getTableRuleConfigs().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        when(regCenter.getDirectly("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        MasterSlaveRuleConfiguration actual = configurationService.loadMasterSlaveRuleConfiguration("sharding_db");
        assertThat(actual.getName(), is("ms_ds"));
    }
    
    @Test
    public void assertLoadAuthentication() {
        when(regCenter.getDirectly("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Authentication actual = configurationService.loadAuthentication();
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertLoadConfigMap() {
        when(regCenter.getDirectly("/test/config/configmap")).thenReturn(CONFIG_MAP_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Map<String, Object> actual = configurationService.loadConfigMap();
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertLoadProperties() {
        when(regCenter.getDirectly("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Properties actual = configurationService.loadProperties();
        assertThat(actual.get(ShardingPropertiesConstant.SQL_SHOW.getKey()), CoreMatchers.<Object>is(Boolean.FALSE));
    }
    
    @Test
    public void assertGetAllShardingSchemaNames() {
        when(regCenter.getChildrenKeys("/test/config/schema")).thenReturn(Arrays.asList("sharding_db", "masterslave_db"));
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Collection<String> actual = configurationService.getAllShardingSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("masterslave_db"));
    }
    
    @Test
    public void assertGetAllSlaveDataSourceNames() {
        when(regCenter.getChildrenKeys("/test/config/schema")).thenReturn(Arrays.asList("sharding_ms_db", "masterslave_db"));
        when(regCenter.getDirectly("/test/config/schema/sharding_ms_db/rule")).thenReturn(SHARDING_MASTER_SLAVE_RULE_YAML);
        when(regCenter.getDirectly("/test/config/schema/masterslave_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigurationService configurationService = new ConfigurationService("test", regCenter);
        Map<String, Collection<String>> actual = configurationService.getAllSlaveDataSourceNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsValue(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertTrue(actual.containsValue(Arrays.asList("db0_slave", "db1_slave")));
    }
}
