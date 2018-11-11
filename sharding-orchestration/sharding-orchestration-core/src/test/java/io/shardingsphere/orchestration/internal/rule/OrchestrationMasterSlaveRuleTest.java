/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.rule;

import io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.orchestration.internal.state.event.DisabledStateEventBusEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrchestrationMasterSlaveRuleTest {
    
    private OrchestrationMasterSlaveRule orchestrationMasterSlaveRule;
    
    @Before
    public void setUp() {
        orchestrationMasterSlaveRule = new OrchestrationMasterSlaveRule(getMasterSlaveRuleConfiguration());
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration result = new MasterSlaveRuleConfiguration();
        result.setName("test_ms");
        result.setLoadBalanceAlgorithm(new RandomMasterSlaveLoadBalanceAlgorithm());
        result.setMasterDataSourceName("master_db");
        result.setSlaveDataSourceNames(Arrays.asList("slave_db_0", "slave_db_1"));
        return result;
    }
    
    @Test
    public void assertGetSlaveDataSourceNames() {
        Collection<String> expected = Arrays.asList("slave_db_0", "slave_db_1");
        assertThat(orchestrationMasterSlaveRule.getSlaveDataSourceNames(), is(expected));
    }
    
    @Test
    public void assertRenew() {
        Collection<String> expected = Collections.singletonList("slave_db_1");
        orchestrationMasterSlaveRule.renew(getDisabledStateEventBusEvent());
        assertThat(orchestrationMasterSlaveRule.getSlaveDataSourceNames(), is(expected));
    }
    
    private DisabledStateEventBusEvent getDisabledStateEventBusEvent() {
        Collection<String> slaveNames = Collections.singletonList("slave_db_0");
        return new DisabledStateEventBusEvent(Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, slaveNames));
    }
}
