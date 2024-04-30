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
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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

class MaskRuleConfigurationRepositoryTupleSwapperTest {
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyRule() {
        assertTrue(new MaskRuleConfigurationRepositoryTupleSwapper().swapToRepositoryTuples(new YamlMaskRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuples() {
        YamlMaskRuleConfiguration yamlRuleConfig = (YamlMaskRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(createMaximumMaskRule());
        Collection<RepositoryTuple> actual = new MaskRuleConfigurationRepositoryTupleSwapper().swapToRepositoryTuples(yamlRuleConfig);
        assertThat(actual.size(), is(2));
        Iterator<RepositoryTuple> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("mask_algorithms/FIXTURE"));
        assertThat(iterator.next().getKey(), is("tables/foo"));
    }
    
    private MaskRuleConfiguration createMaximumMaskRule() {
        Collection<MaskTableRuleConfiguration> tables = Collections.singleton(
                new MaskTableRuleConfiguration("foo", Collections.singleton(new MaskColumnRuleConfiguration("foo_column", "FIXTURE"))));
        return new MaskRuleConfiguration(tables, Collections.singletonMap("FIXTURE", new AlgorithmConfiguration("FIXTURE", new Properties())));
    }
    
    @Test
    void assertSwapToObjectWithEmptyTuple() {
        assertFalse(new MaskRuleConfigurationRepositoryTupleSwapper().swapToObject0(new LinkedList<>()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<RepositoryTuple> repositoryTuples = Arrays.asList(new RepositoryTuple("/metadata/foo_db/rules/mask/tables/foo/versions/0", "columns:\n"
                + "  foo_column:\n"
                + "    logicColumn: foo_column\n"
                + "    maskAlgorithm: FIXTURE\n"
                + "name: foo\n"),
                new RepositoryTuple("/metadata/foo_db/rules/mask/mask_algorithms/FIXTURE/versions/0", "type: FIXTURE\n"));
        Optional<YamlMaskRuleConfiguration> actual = new MaskRuleConfigurationRepositoryTupleSwapper().swapToObject0(repositoryTuples);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(1));
        assertThat(actual.get().getTables().get("foo").getColumns().size(), is(1));
        assertThat(actual.get().getTables().get("foo").getColumns().get("foo_column").getLogicColumn(), is("foo_column"));
        assertThat(actual.get().getTables().get("foo").getColumns().get("foo_column").getMaskAlgorithm(), is("FIXTURE"));
        assertThat(actual.get().getMaskAlgorithms().size(), is(1));
        assertThat(actual.get().getMaskAlgorithms().get("FIXTURE").getType(), is("FIXTURE"));
        assertTrue(actual.get().getMaskAlgorithms().get("FIXTURE").getProps().isEmpty());
    }
}
