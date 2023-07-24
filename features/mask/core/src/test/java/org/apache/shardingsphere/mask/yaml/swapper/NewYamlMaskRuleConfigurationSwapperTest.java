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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewYamlMaskRuleConfigurationSwapperTest {
    
    private final NewYamlMaskRuleConfigurationSwapper swapper = new NewYamlMaskRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        MaskRuleConfiguration config = new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodes() {
        MaskRuleConfiguration config = createMaximumMaskRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(2));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("mask_algorithms/FIXTURE"));
        assertThat(iterator.next().getKey(), is("tables/foo"));
    }
    
    private MaskRuleConfiguration createMaximumMaskRule() {
        Collection<MaskTableRuleConfiguration> tables = new LinkedList<>();
        tables.add(new MaskTableRuleConfiguration("foo", Collections.singleton(new MaskColumnRuleConfiguration("foo_column", "FIXTURE"))));
        return new MaskRuleConfiguration(tables, Collections.singletonMap("FIXTURE", new AlgorithmConfiguration("FIXTURE", new Properties())));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
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
        MaskRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getTables().size(), is(1));
        assertThat(result.getTables().iterator().next().getName(), is("foo"));
        assertThat(result.getTables().iterator().next().getColumns().size(), is(1));
        assertThat(result.getTables().iterator().next().getColumns().iterator().next().getLogicColumn(), is("foo_column"));
        assertThat(result.getTables().iterator().next().getColumns().iterator().next().getMaskAlgorithm(), is("FIXTURE"));
        assertThat(result.getMaskAlgorithms().size(), is(1));
        assertThat(result.getMaskAlgorithms().get("FIXTURE").getType(), is("FIXTURE"));
        assertThat(result.getMaskAlgorithms().get("FIXTURE").getProps().size(), is(0));
    }
}
