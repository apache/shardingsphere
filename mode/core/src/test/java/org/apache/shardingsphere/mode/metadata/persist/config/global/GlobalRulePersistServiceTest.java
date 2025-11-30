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
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.rule.global.MockedGlobalRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalRulePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private GlobalRulePersistService globalRulePersistService;
    
    @BeforeEach
    void setUp() {
        globalRulePersistService = new GlobalRulePersistService(repository, new VersionPersistService(repository));
    }
    
    @Test
    void assertLoadAll() {
        when(repository.getChildrenKeys("/rules")).thenReturn(Collections.singletonList("global_fixture"));
        when(repository.query("/rules/global_fixture/active_version")).thenReturn("0");
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: foo_value");
        List<RuleConfiguration> actual = new ArrayList<>(globalRulePersistService.load());
        assertThat(actual.size(), is(1));
        assertThat(((MockedGlobalRuleConfiguration) actual.get(0)).getName(), is("foo_value"));
    }
    
    @Test
    void assertLoad() {
        when(repository.query("/rules/global_fixture/active_version")).thenReturn("0");
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: foo_value");
        RuleConfiguration actual = globalRulePersistService.load("global_fixture");
        assertThat(((MockedGlobalRuleConfiguration) actual).getName(), is("foo_value"));
    }
    
    @Test
    void assertLoadWithoutVersions() {
        assertThrows(NullPointerException.class, () -> globalRulePersistService.load("global_fixture"));
    }
    
    @Test
    void assertPersistWithVersions() {
        when(repository.getChildrenKeys("/rules/global_fixture/versions")).thenReturn(Collections.singletonList("10"));
        globalRulePersistService.persist(Collections.singleton(new MockedGlobalRuleConfiguration("foo_value")));
        verify(repository).persist("/rules/global_fixture/versions/11", "name: foo_value" + System.lineSeparator());
        verify(repository, never()).persist("/rules/global_fixture/active_version", "0");
    }
    
    @Test
    void assertPersistWithoutVersions() {
        when(repository.getChildrenKeys("/rules/global_fixture/versions")).thenReturn(Collections.emptyList());
        globalRulePersistService.persist(Collections.singleton(new MockedGlobalRuleConfiguration("foo_value")));
        verify(repository).persist("/rules/global_fixture/versions/0", "name: foo_value" + System.lineSeparator());
        verify(repository).persist("/rules/global_fixture/active_version", "0");
    }
}
