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

package org.apache.shardingsphere.mode.metadata.persist.service.config.global;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.metadata.persist.fixture.MetaDataYamlRuleConfigurationFixture;
import org.apache.shardingsphere.mode.metadata.persist.service.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OrderedSPILoader.class)
class GlobalRulePersistServiceTest {
    
    private GlobalRulePersistService globalRulePersistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @Mock
    private RepositoryTuplePersistService repositoryTuplePersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        globalRulePersistService = new GlobalRulePersistService(repository, metaDataVersionPersistService);
        Plugins.getMemberAccessor().set(GlobalRulePersistService.class.getDeclaredField("repositoryTuplePersistService"), globalRulePersistService, repositoryTuplePersistService);
    }
    
    @Test
    void assertLoad() {
        assertTrue(globalRulePersistService.load().isEmpty());
        verify(repositoryTuplePersistService).load("/rules");
    }
    
    @Test
    void assertLoadWithRuleTypeName() {
        assertFalse(globalRulePersistService.load("foo_rule").isPresent());
        verify(repositoryTuplePersistService).load("/rules/foo_rule");
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertPersistWithVersions() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        YamlRuleConfigurationSwapper swapper = mock(YamlRuleConfigurationSwapper.class);
        when(OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig))).thenReturn(Collections.singletonMap(ruleConfig, swapper));
        YamlRuleConfiguration yamlRuleConfig = new MetaDataYamlRuleConfigurationFixture();
        when(swapper.swapToYamlConfiguration(ruleConfig)).thenReturn(yamlRuleConfig);
        when(repository.query("/rules/fixture/active_version")).thenReturn("10");
        when(repository.getChildrenKeys("/rules/fixture/versions")).thenReturn(Collections.singletonList("10"));
        globalRulePersistService.persist(Collections.singleton(ruleConfig));
        verify(repository).persist("/rules/fixture/versions/11", "{}" + System.lineSeparator());
        verify(repository, times(0)).persist("/rules/fixture/active_version", "0");
        verify(repository, times(2)).query("/rules/fixture/active_version");
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertPersistWithoutVersions() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        YamlRuleConfigurationSwapper swapper = mock(YamlRuleConfigurationSwapper.class);
        when(OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig))).thenReturn(Collections.singletonMap(ruleConfig, swapper));
        YamlRuleConfiguration yamlRuleConfig = new MetaDataYamlRuleConfigurationFixture();
        when(swapper.swapToYamlConfiguration(ruleConfig)).thenReturn(yamlRuleConfig);
        when(repository.getChildrenKeys("/rules/fixture/versions")).thenReturn(Collections.emptyList());
        when(repository.query("/rules/fixture/active_version")).thenReturn("", "0");
        globalRulePersistService.persist(Collections.singleton(ruleConfig));
        verify(repository).persist("/rules/fixture/versions/0", "{}" + System.lineSeparator());
        verify(repository).persist("/rules/fixture/active_version", "0");
        verify(repository, times(2)).query("/rules/fixture/active_version");
    }
}
