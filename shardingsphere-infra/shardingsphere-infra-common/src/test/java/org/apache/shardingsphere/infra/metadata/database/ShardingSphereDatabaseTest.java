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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereDatabaseTest {
    
    @Test
    public void assertIsComplete() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mock(RuleConfiguration.class)), Collections.singleton(mock(ShardingSphereRule.class)));
        assertTrue(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resource, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutRule() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList());
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resource, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutDataSource() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.emptyMap());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mock(RuleConfiguration.class)), Collections.singleton(mock(ShardingSphereRule.class)));
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resource, ruleMetaData, Collections.emptyMap()).isComplete());
    }
}
