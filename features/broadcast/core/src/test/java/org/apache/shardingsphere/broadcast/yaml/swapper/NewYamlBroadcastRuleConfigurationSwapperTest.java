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
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewYamlBroadcastRuleConfigurationSwapperTest {
    
    private final NewYamlBroadcastRuleConfigurationSwapper swapper = new NewYamlBroadcastRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        BroadcastRuleConfiguration config = new BroadcastRuleConfiguration(Collections.emptyList());
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodes() {
        BroadcastRuleConfiguration config = createMaximumBroadcastRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(1));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("tables"));
    }
    
    private BroadcastRuleConfiguration createMaximumBroadcastRule() {
        return new BroadcastRuleConfiguration(Arrays.asList("foo_table", "foo_table2"));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/broadcast/tables", "tables:\n"
                + "- foo_table\n"
                + "- foo_table2\n"));
        BroadcastRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getTables().size(), is(2));
        Iterator<String> iterator = result.getTables().iterator();
        assertThat(iterator.next(), is("foo_table"));
        assertThat(iterator.next(), is("foo_table2"));
    }
}
