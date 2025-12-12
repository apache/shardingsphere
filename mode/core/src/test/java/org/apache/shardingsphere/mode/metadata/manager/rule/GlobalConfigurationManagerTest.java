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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.listener.StatisticsCollectJobCronUpdateListener;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereServiceLoader.class, GlobalRulesBuilder.class})
class GlobalConfigurationManagerTest {
    
    @Test
    void assertAlterGlobalRuleConfigurationWithNullValue() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        GlobalConfigurationManager manager = new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class));
        manager.alterGlobalRuleConfiguration(null);
        verify(metaDataContexts, never()).update(any(ShardingSphereMetaData.class), any(MetaDataPersistFacade.class));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationReplacesRules() {
        RuleConfiguration newConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class)));
        ShardingSphereRule remainedRule = mock(ShardingSphereRule.class);
        when(remainedRule.getConfiguration()).thenReturn(mock(RuleConfiguration.class, withSettings().extraInterfaces(Cloneable.class)));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getMetaData()).thenReturn(
                createMetaData(new RuleMetaData(new LinkedList<>(Arrays.asList(closableRule, remainedRule))), new TemporaryConfigurationProperties(new Properties())));
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        when(GlobalRulesBuilder.buildSingleRules(eq(newConfig), any(), any(ConfigurationProperties.class))).thenReturn(Collections.singleton(builtRule));
        GlobalConfigurationManager manager = new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class));
        assertDoesNotThrow(() -> manager.alterGlobalRuleConfiguration(newConfig));
        assertDoesNotThrow(() -> verify((AutoCloseable) closableRule).close());
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules();
        verify(metaDataContexts).update(any(ShardingSphereMetaData.class), any(MetaDataPersistFacade.class));
        assertTrue(rules.contains(builtRule));
        assertTrue(rules.contains(remainedRule));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationRemovesNonClosableAssignableRule() {
        RuleConfiguration newConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule nonClosableAssignableRule = mock(ShardingSphereRule.class);
        when(nonClosableAssignableRule.getConfiguration()).thenReturn(mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class)));
        ShardingSphereMetaData metaData = createMetaData(new RuleMetaData(new LinkedList<>(Collections.singleton(nonClosableAssignableRule))), new TemporaryConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData()).thenReturn(metaData);
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        when(GlobalRulesBuilder.buildSingleRules(eq(newConfig), any(), any(ConfigurationProperties.class))).thenReturn(Collections.singleton(builtRule));
        new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class)).alterGlobalRuleConfiguration(newConfig);
        org.junit.jupiter.api.Assertions.assertFalse(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().contains(nonClosableAssignableRule));
        assertTrue(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().contains(builtRule));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationThrowsWhenCloseFailed() throws Exception {
        RuleConfiguration newConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(newConfig);
        doThrow(Exception.class).when((AutoCloseable) closableRule).close();
        RuleMetaData globalRuleMetaData = new RuleMetaData(new LinkedList<>(Collections.singleton(closableRule)));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData()).thenReturn(createMetaData(globalRuleMetaData, new TemporaryConfigurationProperties(new Properties())));
        when(GlobalRulesBuilder.buildSingleRules(eq(newConfig), any(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        assertThrows(Exception.class, () -> new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class)).alterGlobalRuleConfiguration(newConfig));
    }
    
    @Test
    void assertAlterPropertiesWithCronChanged() {
        ShardingSphereMetaData metaData = createMetaData(new RuleMetaData(Collections.emptyList()),
                new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0/10 * * * * ?"))));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData()).thenReturn(metaData);
        StatisticsCollectJobCronUpdateListener listener = mock(StatisticsCollectJobCronUpdateListener.class);
        when(ShardingSphereServiceLoader.getServiceInstances(StatisticsCollectJobCronUpdateListener.class)).thenReturn(Collections.singleton(listener));
        GlobalConfigurationManager manager = new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class));
        manager.alterProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0/20 * * * * ?")));
        verify(metaDataContexts).update(any(ShardingSphereMetaData.class), any(MetaDataPersistFacade.class));
        verify(listener).updated();
    }
    
    @Test
    void assertAlterPropertiesWithoutCronChange() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON)).thenReturn("0/10 * * * * ?");
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.emptyList());
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        GlobalConfigurationManager manager = new GlobalConfigurationManager(metaDataContexts, mock(MetaDataPersistFacade.class));
        manager.alterProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0/10 * * * * ?")));
        verify(metaDataContexts).update(any(ShardingSphereMetaData.class), any(MetaDataPersistFacade.class));
    }
    
    private ShardingSphereMetaData createMetaData(final RuleMetaData globalRuleMetaData, final TemporaryConfigurationProperties temporaryProps) {
        ShardingSphereMetaData result = new ShardingSphereMetaData(Collections.emptyList(), mock(ResourceMetaData.class), globalRuleMetaData, new ConfigurationProperties(new Properties()));
        result.getTemporaryProps().getProps().putAll(temporaryProps.getProps());
        return result;
    }
}
