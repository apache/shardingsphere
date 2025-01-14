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

package org.apache.shardingsphere.mask.yaml;

import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperEngineIT;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskConfigurationRepositoryTupleSwapperEngineIT extends RepositoryTupleSwapperEngineIT {
    
    MaskConfigurationRepositoryTupleSwapperEngineIT() {
        super("yaml/mask-rule.yaml");
    }
    
    @Override
    protected void assertRepositoryTuples(final List<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(3));
        assertRepositoryTuple(actualRepositoryTuples.get(0),
                "mask_algorithms/keep_first_n_last_m_mask", ((YamlMaskRuleConfiguration) expectedYamlRuleConfig).getMaskAlgorithms().get("keep_first_n_last_m_mask"));
        assertRepositoryTuple(actualRepositoryTuples.get(1), "mask_algorithms/md5_mask", ((YamlMaskRuleConfiguration) expectedYamlRuleConfig).getMaskAlgorithms().get("md5_mask"));
        assertRepositoryTuple(actualRepositoryTuples.get(2), "tables/t_user", ((YamlMaskRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_user"));
    }
}
