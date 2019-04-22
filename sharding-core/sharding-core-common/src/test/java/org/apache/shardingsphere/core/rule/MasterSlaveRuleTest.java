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

package org.apache.shardingsphere.core.rule;

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class MasterSlaveRuleTest {
    
    @Test
    public void assertContainDataSourceNameWithMasterDataSourceName() {
        MasterSlaveRule actual = new MasterSlaveRule(new MasterSlaveRuleConfiguration("master_slave", "master_ds", Collections.singletonList("slave_ds")));
        assertTrue(actual.containDataSourceName("master_ds"));
    }
    
    @Test
    public void assertContainDataSourceNameWithSlaveDataSourceName() {
        MasterSlaveRule actual = new MasterSlaveRule(new MasterSlaveRuleConfiguration("master_slave", "master_ds", Collections.singletonList("slave_ds")));
        assertTrue(actual.containDataSourceName("slave_ds"));
    }
    
    @Test
    public void assertNotContainDataSourceName() {
        MasterSlaveRule actual = new MasterSlaveRule(new MasterSlaveRuleConfiguration("master_slave", "master_ds", Collections.singletonList("slave_ds")));
        assertFalse(actual.containDataSourceName("master_slave"));
    }
}
