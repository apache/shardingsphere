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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.YamlRuleAlteredJobConfiguration;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RuleAlteredJobConfigurationTest {
    
    private static final RuleAlteredJobConfigurationSwapper JOB_CONFIG_SWAPPER = new RuleAlteredJobConfigurationSwapper();
    
    @Test
    public void assertGetJobShardingCountByNull() {
        YamlRuleAlteredJobConfiguration yamlJobConfig = new YamlRuleAlteredJobConfiguration();
        RuleAlteredJobConfiguration jobConfig = JOB_CONFIG_SWAPPER.swapToObject(yamlJobConfig);
        assertThat(jobConfig.getJobShardingCount(), is(0));
    }
    
    @Test
    public void assertGetJobShardingCount() {
        YamlRuleAlteredJobConfiguration yamlJobConfig = new YamlRuleAlteredJobConfiguration();
        yamlJobConfig.setJobShardingDataNodes(Arrays.asList("node1", "node2"));
        RuleAlteredJobConfiguration jobConfig = JOB_CONFIG_SWAPPER.swapToObject(yamlJobConfig);
        assertThat(jobConfig.getJobShardingCount(), is(2));
    }
    
    @Test
    public void assertSplitLogicTableNames() {
        YamlRuleAlteredJobConfiguration yamlJobConfig = new YamlRuleAlteredJobConfiguration();
        yamlJobConfig.setLogicTables("foo_tbl,bar_tbl");
        RuleAlteredJobConfiguration jobConfig = JOB_CONFIG_SWAPPER.swapToObject(yamlJobConfig);
        assertThat(jobConfig.splitLogicTableNames(), is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
}
