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

package org.apache.shardingsphere.sharding.metadata.nodepath;

import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingRuleNodePathProviderTest {
    
    @Test
    void assertNew() {
        RuleNodePathProvider ruleNodePathProvider = new ShardingRuleNodePathProvider();
        RuleNodePath actualRuleNodePath = ruleNodePathProvider.getRuleNodePath();
        assertThat(actualRuleNodePath.getNamedItems().size(), is(6));
        List<String> namedRuleItems = Arrays.asList(ShardingRuleNodePathProvider.TABLES, ShardingRuleNodePathProvider.AUTO_TABLES, ShardingRuleNodePathProvider.BINDING_TABLES,
                ShardingRuleNodePathProvider.ALGORITHMS, ShardingRuleNodePathProvider.KEY_GENERATORS, ShardingRuleNodePathProvider.AUDITORS);
        assertThat("Named rule items equality without order", actualRuleNodePath.getNamedItems().keySet(), IsIterableContainingInAnyOrder.containsInAnyOrder(namedRuleItems.toArray()));
        assertThat(actualRuleNodePath.getUniqueItems().size(), is(6));
        List<String> uniqueRuleItems = Arrays.asList(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY, ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY,
                ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY, ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY, ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN,
                ShardingRuleNodePathProvider.SHARDING_CACHE);
        assertThat("Unique rule items equality without order", actualRuleNodePath.getUniqueItems().keySet(), IsIterableContainingInAnyOrder.containsInAnyOrder(uniqueRuleItems.toArray()));
        assertThat(actualRuleNodePath.getRoot().getRuleType(), is(ShardingRuleNodePathProvider.RULE_TYPE));
    }
}
