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

import org.apache.shardingsphere.mode.node.rule.tuple.RuleRepositoryTuple;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleDatabaseRuleRepositoryTuplePersistServiceTest {
    
    private DatabaseRuleRepositoryTuplePersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new DatabaseRuleRepositoryTuplePersistService(repository);
    }
    
    @Test
    void assertLoadWithChildrenPath() {
        when(repository.getChildrenKeys("/metadata/foo_db/rules")).thenReturn(Collections.singletonList("database_rule_fixture"));
        when(repository.query("/metadata/foo_db/rules/database_rule_fixture/unique/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/rules/database_rule_fixture/unique/versions/0")).thenReturn("unique_content");
        when(repository.getChildrenKeys("/metadata/foo_db/rules/database_rule_fixture/named")).thenReturn(Collections.singletonList("rule_item"));
        when(repository.query("/metadata/foo_db/rules/database_rule_fixture/named/rule_item/active_version")).thenReturn("0");
        when(repository.query("/metadata/foo_db/rules/database_rule_fixture/named/rule_item/versions/0")).thenReturn("named_content");
        List<RuleRepositoryTuple> actual = new ArrayList<>(persistService.load("foo_db"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getKey(), is("/metadata/foo_db/rules/database_rule_fixture/unique"));
        assertThat(actual.get(0).getValue(), is("unique_content"));
        assertThat(actual.get(1).getKey(), is("/metadata/foo_db/rules/database_rule_fixture/named/rule_item"));
        assertThat(actual.get(1).getValue(), is("named_content"));
    }
    
    @Test
    void assertLoadWithoutChildrenPath() {
        when(repository.getChildrenKeys("/metadata/foo_db/rules")).thenReturn(Collections.emptyList());
        Collection<RuleRepositoryTuple> actual = persistService.load("foo_db");
        assertTrue(actual.isEmpty());
    }
}
