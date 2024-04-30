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

package org.apache.shardingsphere.broadcast.yaml.swapper;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastRuleConfigurationRepositoryTupleSwapperTest {
    
    private final BroadcastRuleConfigurationRepositoryTupleSwapper swapper = new BroadcastRuleConfigurationRepositoryTupleSwapper();
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyRule() {
        assertTrue(swapper.swapToRepositoryTuples(new YamlBroadcastRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuples() {
        BroadcastRuleConfiguration ruleConfig = new BroadcastRuleConfiguration(Arrays.asList("foo_table", "foo_table2"));
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples((YamlBroadcastRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(ruleConfig));
        assertThat(actual.size(), is(1));
        Iterator<RepositoryTuple> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("tables"));
    }
    
    @Test
    void assertSwapToObjectWithEmptyTuple() {
        assertFalse(swapper.swapToObject(Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        RepositoryTuple repositoryTuple = new RepositoryTuple("/metadata/foo_db/rules/broadcast/tables", "tables:\n- foo_table\n- foo_table2\n");
        Optional<BroadcastRuleConfiguration> actual = swapper.swapToObject(Collections.singleton(repositoryTuple));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(2));
        Iterator<String> iterator = actual.get().getTables().iterator();
        assertThat(iterator.next(), is("foo_table"));
        assertThat(iterator.next(), is("foo_table2"));
    }
}
