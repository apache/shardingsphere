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

package org.apache.shardingsphere.single.it;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SingleRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    SingleRuleConfigurationYamlIT() {
        super("yaml/single-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertSingleRule((YamlSingleRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertSingleRule(final YamlSingleRuleConfiguration actual) {
        assertThat(actual.getTables(), is(Collections.singletonList("foo_tbl")));
        assertThat(actual.getDefaultDataSource(), is("foo_ds"));
    }
}
