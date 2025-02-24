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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.node.path.type.config.database.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.spi.DatabaseRuleNodeProvider;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingRuleNodePathProviderTest {
    
    private final DatabaseRuleNodeProvider pathProvider = TypedSPILoader.getService(DatabaseRuleNodeProvider.class, ShardingRuleConfiguration.class);
    
    @Test
    void assertGetDatabaseRuleNode() {
        DatabaseRuleNode actual = pathProvider.getDatabaseRuleNode();
        assertThat(actual.getNamedItems().size(), is(6));
        List<String> namedRuleItems = Arrays.asList(ShardingRuleNodeProvider.TABLES, ShardingRuleNodeProvider.AUTO_TABLES, ShardingRuleNodeProvider.BINDING_TABLES,
                ShardingRuleNodeProvider.SHARDING_ALGORITHMS, ShardingRuleNodeProvider.KEY_GENERATORS, ShardingRuleNodeProvider.AUDITORS);
        assertThat("Named rule items equality without order", actual.getNamedItems().keySet(), IsIterableContainingInAnyOrder.containsInAnyOrder(namedRuleItems.toArray()));
        assertThat(actual.getUniqueItems().size(), is(6));
        List<String> uniqueRuleItems = Arrays.asList(ShardingRuleNodeProvider.DEFAULT_DATABASE_STRATEGY, ShardingRuleNodeProvider.DEFAULT_TABLE_STRATEGY,
                ShardingRuleNodeProvider.DEFAULT_KEY_GENERATE_STRATEGY, ShardingRuleNodeProvider.DEFAULT_AUDIT_STRATEGY, ShardingRuleNodeProvider.DEFAULT_SHARDING_COLUMN,
                ShardingRuleNodeProvider.SHARDING_CACHE);
        assertThat("Unique rule items equality without order", actual.getUniqueItems().keySet(), IsIterableContainingInAnyOrder.containsInAnyOrder(uniqueRuleItems.toArray()));
        assertThat(actual.getRuleType(), is(ShardingRuleNodeProvider.RULE_TYPE));
    }
}
