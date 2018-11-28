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

package io.shardingsphere.orchestration.internal;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.config.listener.ConfigurationOrchestrationListenerManager;
import io.shardingsphere.orchestration.internal.listener.OrchestrationListenerManager;
import io.shardingsphere.orchestration.internal.state.listener.StateOrchestrationListenerManager;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.listener.EventListener;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationFacadeTest {
    
    private OrchestrationFacade orchestrationFacade;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        orchestrationFacade = new OrchestrationFacade(getOrchestrationConfiguration(), Arrays.asList("sharding_db", "masterslave_db"));
        setRegistry(orchestrationFacade);
        setRegCenterForOrchestrationListenerManager();
        setRegistry(orchestrationFacade.getConfigService());
        setRegistry(orchestrationFacade.getClass().getDeclaredField("instanceStateService"), orchestrationFacade);
        setRegistry(orchestrationFacade.getClass().getDeclaredField("dataSourceService"), orchestrationFacade);
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration("test", new RegistryCenterConfiguration(), true);
    }
    
    private void setRegCenterForOrchestrationListenerManager() throws ReflectiveOperationException {
        Field file = orchestrationFacade.getClass().getDeclaredField("listenerManager");
        file.setAccessible(true);
        OrchestrationListenerManager listenerManager = (OrchestrationListenerManager) file.get(orchestrationFacade);
        ConfigurationOrchestrationListenerManager configOrchestrationListenerManager
                = new ConfigurationOrchestrationListenerManager("test", regCenter, Arrays.asList("sharding_db", "masterslave_db"));
        StateOrchestrationListenerManager stateOrchestrationListenerManager = new StateOrchestrationListenerManager("test", regCenter);
        setField(listenerManager, "configOrchestrationListenerManager", configOrchestrationListenerManager);
        setField(listenerManager, "stateOrchestrationListenerManager", stateOrchestrationListenerManager);
    }
    
    private void setRegistry(final Field field, final Object target) throws ReflectiveOperationException {
        field.setAccessible(true);
        setRegistry(field.get(target));
    }
    
    private void setRegistry(final Object target) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField("regCenter");
        field.setAccessible(true);
        field.set(target, regCenter);
    }
    
    private void setField(final Object target, final String fieldName, final Object fieldValue) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
    
    @Test
    public void assertInitWithParameters() {
        orchestrationFacade.init(Collections.singletonMap("sharding_db",
                getDataSourceConfigurationMap()), getRuleConfigurationMap(), getAuthentication(), Collections.<String, Object>emptyMap(), getProperties());
        verify(regCenter).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.<String>any());
        verify(regCenter).persistEphemeral(anyString(), anyString());
        verify(regCenter).persist("/test/state/datasources", "");
        verify(regCenter).watch(eq("/test/config/authentication"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/configmap"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/props"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/rule"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/rule"), any(EventListener.class));
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurationMap() {
        return Maps.transformValues(getDataSourceMap(), new Function<DataSource, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final DataSource input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", getDataSource("ds_0"));
        result.put("ds_1", getDataSource("ds_1"));
        return result;
    }
    
    private DataSource getDataSource(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        return Collections.singletonMap("sharding_db", getShardingRuleConfiguration());
    }
    
    private RuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_ms_${0..1}.t_order_${0..1}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        result.getMasterSlaveRuleConfigs().addAll(getMasterSlaveRuleConfigurations());
        result.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_ms_${user_id % 2}"));
        result.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        return result;
    }
    
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        Collection<MasterSlaveRuleConfiguration> result = new LinkedList<>();
        for (int each : Arrays.asList(0, 1)) {
            MasterSlaveRuleConfiguration msConfig = new MasterSlaveRuleConfiguration();
            msConfig.setName("ds_ms_" + String.valueOf(each));
            msConfig.setLoadBalanceAlgorithm(new RandomMasterSlaveLoadBalanceAlgorithm());
            msConfig.setMasterDataSourceName("ds_" + String.valueOf(each));
            msConfig.setSlaveDataSourceNames(Collections.singletonList("ds_" + String.valueOf(each) + "_slave"));
            result.add(msConfig);
        }
        return result;
    }
    
    private Authentication getAuthentication() {
        Authentication result = new Authentication();
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.put(ShardingPropertiesConstant.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertInitWithoutParameters() {
        orchestrationFacade.init();
        verify(regCenter).persistEphemeral(anyString(), anyString());
        verify(regCenter).persist("/test/state/datasources", "");
        verify(regCenter).watch(eq("/test/config/authentication"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/configmap"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/datasource"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/props"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/sharding_db/rule"), any(EventListener.class));
        verify(regCenter).watch(eq("/test/config/schema/masterslave_db/rule"), any(EventListener.class));
    }
    
    @Test
    public void assertClose() throws Exception {
        orchestrationFacade.close();
        verify(regCenter).close();
    }
}
