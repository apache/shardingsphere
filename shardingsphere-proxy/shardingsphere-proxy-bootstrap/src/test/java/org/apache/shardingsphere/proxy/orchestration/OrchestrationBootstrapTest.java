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

package org.apache.shardingsphere.proxy.orchestration;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
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

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OrchestrationBootstrapTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrchestrationFacade orchestrationFacade;
    
    private OrchestrationBootstrap orchestrationBootstrap;
    
    @Before
    public void setUp() {
        orchestrationBootstrap = new OrchestrationBootstrap(orchestrationFacade);
        when(orchestrationFacade.getConfigCenter().getAllSchemaNames()).thenReturn(Collections.singletonList("db"));
        when(orchestrationFacade.getConfigCenter().loadDataSourceConfigurations("db")).thenReturn(Collections.singletonMap("db", new DataSourceConfiguration(HikariDataSource.class.getName())));
    }
    
    @Test
    public void assertInitFromRegistryCenter() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/reg_center/");
        assertProxyConfiguration(orchestrationBootstrap.init(new YamlProxyConfiguration(yamlProxyConfig.getServerConfiguration(), yamlProxyConfig.getRuleConfigurations())));
    }
    
    @Test
    public void assertInitFromLocalConfiguration() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/local");
        assertProxyConfiguration(orchestrationBootstrap.init(new YamlProxyConfiguration(yamlProxyConfig.getServerConfiguration(), yamlProxyConfig.getRuleConfigurations())));
    }
    
    private void assertProxyConfiguration(final ProxyConfiguration actual) {
        // TODO
    }
}
