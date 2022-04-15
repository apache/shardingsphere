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

package org.apache.shardingsphere.sharding;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.schedule.ShardingRuleAlteredDetector;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ShardingRuleAlteredDetectorTest {
    
    @Test
    public void assertGetOnRuleAlteredActionConfigSuccess() {
        assertFalse(new ShardingRuleAlteredDetector().getOnRuleAlteredActionConfig(new ShardingRuleConfiguration()).isPresent());
    }
    
    @Test
    public void assertFindRuleAlteredLogicTablesSucceed() throws IOException {
        URL sourceUrl = getClass().getClassLoader().getResource("scaling/detector/source_rule_config.yaml");
        assertNotNull(sourceUrl);
        YamlRuleConfiguration sourceRuleConfig = YamlEngine.unmarshal(new File(sourceUrl.getFile()), YamlShardingRuleConfiguration.class);
        URL targetUrl = getClass().getClassLoader().getResource("scaling/detector/target_rule_config.yaml");
        assertNotNull(targetUrl);
        YamlRuleConfiguration targetRuleConfig = YamlEngine.unmarshal(new File(targetUrl.getFile()), YamlShardingRuleConfiguration.class);
        Map<String, Map<String, Object>> sameDataSources = new HashMap<>(5, 1);
        for (int i = 0; i < 5; i++) {
            Map<String, Object> props = new HashMap<>(2, 1);
            props.put("dataSourceClassName", "org.apache.shardingsphere.test.mock.MockedDataSource");
            props.put("jdbcUrl", "jdbc:h2:mem:test_ds_" + i + ";DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
            sameDataSources.put("ds_" + i, props);
        }
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(sourceRuleConfig, targetRuleConfig, sameDataSources, sameDataSources);
        assertThat(ruleAlteredLogicTables.get(0), Matchers.is("t_order"));
    }
    
    @Test
    public void assertNoFindRuleAlteredLogicTables() throws IOException {
        URL sourceUrl = getClass().getClassLoader().getResource("scaling/detector/source_rule_config.yaml");
        assertNotNull(sourceUrl);
        YamlRuleConfiguration sourceRuleConfig = YamlEngine.unmarshal(new File(sourceUrl.getFile()), YamlShardingRuleConfiguration.class);
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(sourceRuleConfig, sourceRuleConfig, null, null);
        assertThat("not table rule alter", ruleAlteredLogicTables.size(), Matchers.is(0));
    }
    
    @Test
    public void assertExtractAllLogicTables() throws IOException {
        URL sourceUrl = getClass().getClassLoader().getResource("scaling/detector/source_rule_config.yaml");
        assertNotNull(sourceUrl);
        YamlRuleConfiguration sourceRuleConfig = YamlEngine.unmarshal(new File(sourceUrl.getFile()), YamlShardingRuleConfiguration.class);
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(sourceRuleConfig, null, null, null);
        assertThat(ruleAlteredLogicTables.get(0), Matchers.is("t_order"));
    }
}
