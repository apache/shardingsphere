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

package org.apache.shardingsphere.governance.core.facade;

import org.apache.shardingsphere.governance.core.config.ConfigCenter;
import org.apache.shardingsphere.governance.core.facade.listener.GovernanceListenerManager;
import org.apache.shardingsphere.governance.core.facade.repository.GovernanceRepositoryFacade;
import org.apache.shardingsphere.governance.core.facade.util.FieldUtil;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceFacadeTest {
    
    private final GovernanceFacade governanceFacade = new GovernanceFacade();
    
    @Mock
    private GovernanceRepositoryFacade repositoryFacade;
    
    @Mock
    private ConfigCenter configCenter;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private GovernanceListenerManager listenerManager;
    
    @Before
    public void setUp() {
        GovernanceConfiguration governanceConfiguration = new GovernanceConfiguration("test_name", new GovernanceCenterConfiguration("ALL", "127.0.0.1", new Properties()), false);
        governanceFacade.init(governanceConfiguration, Arrays.asList("sharding_db", "masterslave_db"));
        FieldUtil.setField(governanceFacade, "repositoryFacade", repositoryFacade);
        FieldUtil.setField(governanceFacade, "configCenter", configCenter);
        FieldUtil.setField(governanceFacade, "registryCenter", registryCenter);
        FieldUtil.setField(governanceFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertOnlineInstanceWithParameters() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = Collections.singletonMap("test_ds", mock(DataSourceConfiguration.class));
        Map<String, Collection<RuleConfiguration>> ruleConfigurationMap = Collections.singletonMap("sharding_db", Collections.singletonList(mock(RuleConfiguration.class)));
        ProxyUser proxyUser = new ProxyUser("root", Collections.singleton("db1"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Properties props = new Properties();
        governanceFacade.onlineInstance(Collections.singletonMap("sharding_db", dataSourceConfigurationMap), ruleConfigurationMap, authentication, props);
        verify(configCenter).persistConfigurations("sharding_db", dataSourceConfigurationMap, ruleConfigurationMap.get("sharding_db"), false);
        verify(configCenter).persistGlobalConfiguration(authentication, props, false);
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataNodes();
        verify(listenerManager).init();
    }
    
    @Test
    public void assertOnlineInstanceWithoutParameters() {
        governanceFacade.onlineInstance();
        verify(registryCenter).persistInstanceOnline();
        verify(registryCenter).persistDataNodes();
        verify(listenerManager).init();
    }
    
    @Test
    public void assertClose() {
        governanceFacade.close();
        verify(repositoryFacade).close();
    }
}
