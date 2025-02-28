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

package org.apache.shardingsphere.mode.metadata.persist.config.global;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.fixture.infra.rule.global.MockedGlobalRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalRulePersistServiceTest {
    
    private GlobalRulePersistService globalRulePersistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @Mock
    private GlobalRuleRepositoryTuplePersistService ruleRepositoryTuplePersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        globalRulePersistService = new GlobalRulePersistService(repository, metaDataVersionPersistService);
        Plugins.getMemberAccessor().set(GlobalRulePersistService.class.getDeclaredField("ruleRepositoryTuplePersistService"), globalRulePersistService, ruleRepositoryTuplePersistService);
    }
    
    @Test
    void assertLoad() {
        assertTrue(globalRulePersistService.load().isEmpty());
        verify(ruleRepositoryTuplePersistService).load();
    }
    
    @Test
    void assertLoadWithRuleType() {
        when(ruleRepositoryTuplePersistService.load("global_fixture")).thenReturn("name: foo_value");
        assertThat(((MockedGlobalRuleConfiguration) globalRulePersistService.load("global_fixture")).getName(), is("foo_value"));
    }
    
    @Test
    void assertPersistWithVersions() {
        RuleConfiguration ruleConfig = new MockedGlobalRuleConfiguration("foo_value");
        when(repository.getChildrenKeys("/rules/global_fixture/versions")).thenReturn(Collections.singletonList("10"));
        globalRulePersistService.persist(Collections.singleton(ruleConfig));
        verify(repository).persist("/rules/global_fixture/versions/11", "name: foo_value" + System.lineSeparator());
        verify(repository, times(0)).persist("/rules/global_fixture/active_version", "0");
    }
    
    @Test
    void assertPersistWithoutVersions() {
        when(repository.getChildrenKeys("/rules/global_fixture/versions")).thenReturn(Collections.emptyList());
        globalRulePersistService.persist(Collections.singleton(new MockedGlobalRuleConfiguration("foo_value")));
        verify(repository).persist("/rules/global_fixture/versions/0", "name: foo_value" + System.lineSeparator());
        verify(repository).persist("/rules/global_fixture/active_version", "0");
    }
}
