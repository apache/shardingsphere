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

package org.apache.shardingsphere.readwritesplitting.yaml;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReadwriteSplittingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ReadwriteSplittingRuleConfigurationYamlIT() {
        super("yaml/readwrite-splitting-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertReadwriteSplittingRule((YamlReadwriteSplittingRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertReadwriteSplittingRule(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(2));
        assertReadwriteSplittingRuleForDs0(actual);
        assertReadwriteSplittingRuleForDs1(actual);
    }
    
    private void assertReadwriteSplittingRuleForDs0(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertNotNull(actual.getDataSources().get("ds_0"));
        YamlReadwriteSplittingDataSourceRuleConfiguration config = actual.getDataSources().get("ds_0");
        assertThat(config.getWriteDataSourceName(), is("write_ds_0"));
        assertThat(actual.getDataSources().get("ds_0").getLoadBalancerName(), is("roundRobin"));
    }
    
    private void assertReadwriteSplittingRuleForDs1(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertNotNull(actual.getDataSources().get("ds_1"));
        YamlReadwriteSplittingDataSourceRuleConfiguration config = actual.getDataSources().get("ds_1");
        assertThat(config.getWriteDataSourceName(), is("write_ds_1"));
        assertThat(actual.getDataSources().get("ds_1").getLoadBalancerName(), is("random"));
    }
}
