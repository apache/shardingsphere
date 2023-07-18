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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ShardingSphereDatabaseTest {
    
    @Test
    void assertIsComplete() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertTrue(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutRule() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList());
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutDataSource() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.emptyMap());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertReloadRules() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.singletonMap("ds", new MockedDataSource()));
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(mock(MutableDataNodeRule.class, RETURNS_DEEP_STUBS));
        rules.add(mock(DataSourceContainedRule.class, RETURNS_DEEP_STUBS));
        rules.add(mock(TableContainedRule.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap());
        database.reloadRules(MutableDataNodeRule.class);
        assertThat(database.getRuleMetaData().getRules().size(), is(3));
    }
}
