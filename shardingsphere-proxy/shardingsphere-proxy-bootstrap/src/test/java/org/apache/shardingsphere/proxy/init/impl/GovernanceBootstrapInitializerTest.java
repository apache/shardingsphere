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

package org.apache.shardingsphere.proxy.init.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceBootstrapInitializerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GovernanceFacade governanceFacade;
    
    private GovernanceBootstrapInitializer initializer;
    
    @Before
    public void setUp() {
        initializer = new GovernanceBootstrapInitializer(governanceFacade);
        when(governanceFacade.getConfigCenter().getAllSchemaNames()).thenReturn(Collections.singletonList("db"));
        when(governanceFacade.getConfigCenter().loadDataSourceConfigurations("db")).thenReturn(Collections.singletonMap("db", new DataSourceConfiguration(HikariDataSource.class.getName())));
    }
    
    @Test
    public void assertGetProxyConfiguration() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/reg_center/");
        assertProxyConfiguration(initializer.getProxyConfiguration(new YamlProxyConfiguration(yamlProxyConfig.getServerConfiguration(), yamlProxyConfig.getRuleConfigurations())));
    }
    
    @Test
    public void assertGetProxyConfigurationFromLocalConfiguration() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/local");
        assertProxyConfiguration(initializer.getProxyConfiguration(new YamlProxyConfiguration(yamlProxyConfig.getServerConfiguration(), yamlProxyConfig.getRuleConfigurations())));
    }
    
    private void assertProxyConfiguration(final ProxyConfiguration actual) {
        assertTrue(actual.getSchemaDataSources().containsKey("db"));
        assertTrue(actual.getSchemaRules().containsKey("db"));
    }
}
