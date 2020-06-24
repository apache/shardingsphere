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

package org.apache.shardingsphere.sharding.api.config.strategy.sharding;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComplexShardingStrategyConfigurationTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutShardingColumns() {
        new ComplexShardingStrategyConfiguration("", "test");
    }
    
    @Test(expected = NullPointerException.class)
    public void assertConstructorWithoutShardingAlgorithm() {
        new ComplexShardingStrategyConfiguration("id, creation_date", null);
    }
    
    @Test
    public void assertConstructorWithFullArguments() {
        ComplexShardingStrategyConfiguration actual = new ComplexShardingStrategyConfiguration("id, creation_date", "test");
        assertThat(actual.getShardingColumns(), is("id, creation_date"));
        assertThat(actual.getShardingAlgorithmName(), is("test"));
    }
}
