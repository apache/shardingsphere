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

package org.apache.shardingsphere.mask.it;

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.MaskRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    MaskConfigurationRepositoryTupleSwapperIT() {
        super("yaml/mask-rule.yaml", new MaskRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(3));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertMaskAlgorithms(actual.subList(0, 2), ((YamlMaskRuleConfiguration) expectedYamlRuleConfig).getMaskAlgorithms());
        assertTable(actual.get(2), ((YamlMaskRuleConfiguration) expectedYamlRuleConfig).getTables());
    }
    
    private void assertMaskAlgorithms(final List<RepositoryTuple> actual, final Map<String, YamlAlgorithmConfiguration> expectedMaskAlgorithms) {
        assertRepositoryTuple(actual.get(0), "mask_algorithms/keep_first_n_last_m_mask", expectedMaskAlgorithms.get("keep_first_n_last_m_mask"));
        assertRepositoryTuple(actual.get(1), "mask_algorithms/md5_mask", expectedMaskAlgorithms.get("md5_mask"));
    }
    
    private void assertTable(final RepositoryTuple actual, final Map<String, YamlMaskTableRuleConfiguration> expectedTables) {
        assertRepositoryTuple(actual, "tables/t_user", expectedTables.get("t_user"));
    }
}
