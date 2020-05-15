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

package org.apache.shardingsphere.masterslave.api.config;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceConfigurationTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutName() {
        new MasterSlaveDataSourceConfiguration("", "master_ds", Collections.singletonList("slave_ds"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutMasterDataSourceName() {
        new MasterSlaveDataSourceConfiguration("ds", "", Collections.singletonList("slave_ds"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutSlaveDataSourceNames() {
        new MasterSlaveDataSourceConfiguration("ds", "master_ds", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithEmptySlaveDataSourceNames() {
        new MasterSlaveDataSourceConfiguration("ds", "master_ds", Collections.emptyList());
    }
    
    @Test
    public void assertConstructorWithMinArguments() {
        MasterSlaveDataSourceConfiguration actual = new MasterSlaveDataSourceConfiguration("ds", "master_ds", Collections.singletonList("slave_ds"));
        assertThat(actual.getName(), CoreMatchers.is("ds"));
        assertThat(actual.getMasterDataSourceName(), CoreMatchers.is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.is(Collections.singletonList("slave_ds")));
        assertNull(actual.getLoadBalanceStrategyConfiguration());
    }
    
    @Test
    public void assertConstructorWithMaxArguments() {
        MasterSlaveDataSourceConfiguration actual = new MasterSlaveDataSourceConfiguration(
                "ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN"));
        assertThat(actual.getName(), CoreMatchers.is("ds"));
        assertThat(actual.getMasterDataSourceName(), CoreMatchers.is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.is(Collections.singletonList("slave_ds")));
        assertThat(actual.getLoadBalanceStrategyConfiguration().getType(), CoreMatchers.is("ROUND_ROBIN"));
    }
}
