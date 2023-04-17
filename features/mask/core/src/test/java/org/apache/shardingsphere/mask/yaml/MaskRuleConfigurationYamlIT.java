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

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    MaskRuleConfigurationYamlIT() {
        super("yaml/mask-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertMaskRule((YamlMaskRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertMaskRule(final YamlMaskRuleConfiguration actual) {
        assertTables(actual);
        assertMaskAlgorithm(actual);
    }
    
    private void assertTables(final YamlMaskRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().get("t_user").getColumns().size(), is(2));
    }
    
    private void assertMaskAlgorithm(final YamlMaskRuleConfiguration actual) {
        assertThat(actual.getMaskAlgorithms().size(), is(2));
        assertThat(actual.getTables().get("t_user").getColumns().get("telephone").getMaskAlgorithm(), is("keep_first_n_last_m_mask"));
        assertThat(actual.getMaskAlgorithms().get("keep_first_n_last_m_mask").getType(), is("KEEP_FIRST_N_LAST_M"));
        assertThat(actual.getMaskAlgorithms().get("keep_first_n_last_m_mask").getProps().get("replace-char"), is("*"));
    }
}
