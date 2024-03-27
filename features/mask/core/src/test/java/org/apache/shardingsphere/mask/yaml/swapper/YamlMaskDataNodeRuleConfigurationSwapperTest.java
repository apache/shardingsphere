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

package org.apache.shardingsphere.mask.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlMaskDataNodeRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapEmptyConfigurationToDataNodes() {
        MaskRuleConfiguration config = new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        assertThat(new YamlMaskDataNodeRuleConfigurationSwapper().swapToDataNodes(config).size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigurationToDataNodes() {
        MaskRuleConfiguration config = createMaximumMaskRule();
        Collection<YamlDataNode> actual = new YamlMaskDataNodeRuleConfigurationSwapper().swapToDataNodes(config);
        assertThat(actual.size(), is(2));
        Iterator<YamlDataNode> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("mask_algorithms/FIXTURE"));
        assertThat(iterator.next().getKey(), is("tables/foo"));
    }
    
    private MaskRuleConfiguration createMaximumMaskRule() {
        Collection<MaskTableRuleConfiguration> tables = Collections.singleton(
                new MaskTableRuleConfiguration("foo", Collections.singleton(new MaskColumnRuleConfiguration("foo_column", "FIXTURE"))));
        return new MaskRuleConfiguration(tables, Collections.singletonMap("FIXTURE", new AlgorithmConfiguration("FIXTURE", new Properties())));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        assertFalse(new YamlMaskDataNodeRuleConfigurationSwapper().swapToObject(new LinkedList<>()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/mask/tables/foo/versions/0", "columns:\n"
                + "  foo_column:\n"
                + "    logicColumn: foo_column\n"
                + "    maskAlgorithm: FIXTURE\n"
                + "name: foo\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/mask/mask_algorithms/FIXTURE/versions/0", "type: FIXTURE\n"));
        Optional<MaskRuleConfiguration> actual = new YamlMaskDataNodeRuleConfigurationSwapper().swapToObject(config);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(1));
        assertThat(actual.get().getTables().iterator().next().getName(), is("foo"));
        assertThat(actual.get().getTables().iterator().next().getColumns().size(), is(1));
        assertThat(actual.get().getTables().iterator().next().getColumns().iterator().next().getLogicColumn(), is("foo_column"));
        assertThat(actual.get().getTables().iterator().next().getColumns().iterator().next().getMaskAlgorithm(), is("FIXTURE"));
        assertThat(actual.get().getMaskAlgorithms().size(), is(1));
        assertThat(actual.get().getMaskAlgorithms().get("FIXTURE").getType(), is("FIXTURE"));
        assertThat(actual.get().getMaskAlgorithms().get("FIXTURE").getProps().size(), is(0));
    }
}
