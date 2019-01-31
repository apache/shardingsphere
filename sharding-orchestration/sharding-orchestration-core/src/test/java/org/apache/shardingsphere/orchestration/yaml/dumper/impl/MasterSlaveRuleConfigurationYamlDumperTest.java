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

package org.apache.shardingsphere.orchestration.yaml.dumper.impl;

import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

public final class MasterSlaveRuleConfigurationYamlDumperTest {
    
    @Test
    public void assertDump() {
        String actual = new MasterSlaveRuleConfigurationYamlDumper().dump(createMasterSlaveRuleConfiguration());
        assertTrue(actual.contains("ms_ds"));
    }
    
    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
    }
}
