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

package org.apache.shardingsphere.mode.metadata.persist.config.database;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.node.path.version.MetaDataVersion;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseRulePersistServiceTest {
    
    private DatabaseRulePersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new DatabaseRulePersistService(repository);
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/rules")).thenReturn(Collections.singletonList("fixture"));
        when(repository.query("/metadata/foo_db/rules/fixture/unique/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/rules/fixture/unique/versions/0")).thenReturn("unique_content");
        when(repository.getChildrenKeys("/metadata/foo_db/rules/fixture/named")).thenReturn(Collections.singletonList("rule_item"));
        when(repository.query("/metadata/foo_db/rules/fixture/named/rule_item/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/rules/fixture/named/rule_item/versions/0")).thenReturn("named_content");
        Collection<RuleConfiguration> actual = persistService.load("foo_db");
        assertThat(actual.size(), is(1));
        MockedRuleConfiguration ruleConfig = (MockedRuleConfiguration) actual.iterator().next();
        assertThat(ruleConfig.getUnique(), is("unique_content"));
        assertThat(ruleConfig.getNamed(), is(Collections.singletonMap("rule_item", "named_content")));
    }
    
    @Test
    void assertLoadWithEmptyDatabase() {
        when(repository.getChildrenKeys("/metadata/foo_db/rules")).thenReturn(Collections.emptyList());
        assertTrue(persistService.load("foo_db").isEmpty());
    }
    
    @Test
    void assertPersistWithoutActiveVersion() {
        Collection<MetaDataVersion> actual = persistService.persist("foo_db", Collections.singleton(new MockedRuleConfiguration("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getActiveVersion(), is(0));
    }
    
    @Test
    void assertPersistWithActiveVersion() {
        when(repository.getChildrenKeys("/metadata/foo_db/rules/fixture/unique/versions")).thenReturn(Collections.singletonList("10"));
        Collection<MetaDataVersion> actual = persistService.persist("foo_db", Collections.singleton(new MockedRuleConfiguration("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getActiveVersion(), is(10));
    }
    
    @Test
    void assertDeleteWithRuleType() {
        persistService.delete("foo_db", "foo_rule");
        verify(repository).delete("/metadata/foo_db/rules/foo_rule");
    }
    
    @Test
    void assertDeleteWithRuleConfigurations() {
        Collection<MetaDataVersion> actual = persistService.delete("foo_db", Collections.singleton(new MockedRuleConfiguration("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getActiveVersion(), is(0));
    }
}
