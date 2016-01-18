/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public final class ShardingConfigurationTest {
    
    private final Properties prop = new Properties();
    
    private final ShardingConfiguration shardingConfiguration = new ShardingConfiguration(prop);
    
    @Before
    public void setUp() {
        prop.put(ShardingConfigurationConstant.METRICS_SECOND_PERIOD.getKey(), "1000");
        prop.put(ShardingConfigurationConstant.METRICS_ENABLE.getKey(), "true");
        prop.put(ShardingConfigurationConstant.METRICS_PACKAGE_NAME.getKey(), "example");
    }
    
    @Test
    public void assertGetConfigWithDefaultValue() {
        assertThat(new ShardingConfiguration(new Properties()).getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD),
                is(ShardingConfigurationConstant.METRICS_SECOND_PERIOD.getDefaultValue()));
        assertThat(new ShardingConfiguration(new Properties()).getConfig(ShardingConfigurationConstant.METRICS_ENABLE),
                is(ShardingConfigurationConstant.METRICS_ENABLE.getDefaultValue()));
        assertThat(new ShardingConfiguration(new Properties()).getConfig(ShardingConfigurationConstant.METRICS_PACKAGE_NAME),
                is(ShardingConfigurationConstant.METRICS_PACKAGE_NAME.getDefaultValue()));
    }
    
    @Test
    public void assertGetConfig() {
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD), is("1000"));
    }
    
    @Test
    public void assertGetConfigForBoolean() {
        assertTrue(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_ENABLE, boolean.class));
        assertTrue(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_ENABLE, Boolean.class));
    }
    
    @Test
    public void assertGetConfigForInteger() {
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, int.class), is(1000));
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, Integer.class), is(1000));
    }
    
    @Test
    public void assertGetConfigForLong() {
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, long.class), is(1000L));
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, Long.class), is(1000L));
    }
    
    @Test
    public void assertGetConfigForDouble() {
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, double.class), is(1000D));
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_SECOND_PERIOD, Double.class), is(1000D));
    }
    
    @Test
    public void assertGetConfigForString() {
        assertThat(shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_PACKAGE_NAME, String.class), is("example"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertGetConfigFailure() {
        shardingConfiguration.getConfig(ShardingConfigurationConstant.METRICS_PACKAGE_NAME, Object.class);
    }
}
