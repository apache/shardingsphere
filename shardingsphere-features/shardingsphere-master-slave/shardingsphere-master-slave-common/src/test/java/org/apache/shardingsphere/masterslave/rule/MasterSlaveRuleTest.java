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

package org.apache.shardingsphere.masterslave.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MasterSlaveRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new MasterSlaveRule(new MasterSlaveRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<MasterSlaveDataSourceRule> actual = createMasterSlaveRule().findDataSourceRule("test_ms");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createMasterSlaveRule().getSingleDataSourceRule());
    }
    
    private MasterSlaveRule createMasterSlaveRule() {
        MasterSlaveDataSourceRuleConfiguration configuration = new MasterSlaveDataSourceRuleConfiguration("test_ms", "master_db", Arrays.asList("slave_db_0", "slave_db_1"), "random");
        return new MasterSlaveRule(new MasterSlaveRuleConfiguration(
                Collections.singleton(configuration), ImmutableMap.of("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()))));
    }
    
    private void assertDataSourceRule(final MasterSlaveDataSourceRule actual) {
        assertThat(actual.getName(), is("test_ms"));
        assertThat(actual.getMasterDataSourceName(), is("master_db"));
        assertThat(actual.getSlaveDataSourceNames(), is(Arrays.asList("slave_db_0", "slave_db_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        MasterSlaveRule masterSlaveRule = createMasterSlaveRule();
        masterSlaveRule.updateRuleStatus(new DataSourceNameDisabledEvent("slave_db", true));
        assertThat(masterSlaveRule.getSingleDataSourceRule().getSlaveDataSourceNames(), is(Arrays.asList("slave_db_0", "slave_db_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        MasterSlaveRule masterSlaveRule = createMasterSlaveRule();
        masterSlaveRule.updateRuleStatus(new DataSourceNameDisabledEvent("slave_db_0", true));
        assertThat(masterSlaveRule.getSingleDataSourceRule().getSlaveDataSourceNames(), is(Collections.singletonList("slave_db_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        MasterSlaveRule masterSlaveRule = createMasterSlaveRule();
        masterSlaveRule.updateRuleStatus(new DataSourceNameDisabledEvent("slave_db_0", true));
        assertThat(masterSlaveRule.getSingleDataSourceRule().getSlaveDataSourceNames(), is(Collections.singletonList("slave_db_1")));
        masterSlaveRule.updateRuleStatus(new DataSourceNameDisabledEvent("slave_db_0", false));
        assertThat(masterSlaveRule.getSingleDataSourceRule().getSlaveDataSourceNames(), is(Arrays.asList("slave_db_0", "slave_db_1")));
    }
}
