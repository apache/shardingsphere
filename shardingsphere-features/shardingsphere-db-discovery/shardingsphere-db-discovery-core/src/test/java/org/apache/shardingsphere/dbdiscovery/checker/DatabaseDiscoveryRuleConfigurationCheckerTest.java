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

package org.apache.shardingsphere.dbdiscovery.checker;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDiscoveryRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        DatabaseDiscoveryRuleConfiguration config = getValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singletonList(config)).get(config);
        assertThat(checker, instanceOf(DatabaseDiscoveryRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private DatabaseDiscoveryRuleConfiguration getValidConfiguration() {
        DatabaseDiscoveryRuleConfiguration result = mock(DatabaseDiscoveryRuleConfiguration.class);
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = mock(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        when(dataSourceRuleConfig.getDiscoveryTypeName()).thenReturn("jdbc");
        when(result.getDataSources()).thenReturn(Collections.singletonList(dataSourceRuleConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInvalidCheck() {
        DatabaseDiscoveryRuleConfiguration config = getInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singletonList(config)).get(config);
        assertThat(checker, instanceOf(DatabaseDiscoveryRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private DatabaseDiscoveryRuleConfiguration getInvalidConfiguration() {
        DatabaseDiscoveryRuleConfiguration result = mock(DatabaseDiscoveryRuleConfiguration.class);
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = mock(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        when(dataSourceRuleConfig.getDiscoveryTypeName()).thenReturn("");
        when(result.getDataSources()).thenReturn(Collections.singletonList(dataSourceRuleConfig));
        return result;
    }
}
