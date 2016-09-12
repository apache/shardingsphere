/*
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

package com.dangdang.ddframe.rdb.sharding.config;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPropertiesTest {
    
    private final Properties prop = new Properties();
    
    private ShardingProperties shardingProperties;
    
    @Before
    public void setUp() {
        prop.put(ShardingPropertiesConstant.METRICS_ENABLE.getKey(), "true");
        prop.put(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD.getKey(), "1000");
        prop.put(ShardingPropertiesConstant.METRICS_LOGGER_NAME.getKey(), "example");
        prop.put(ShardingPropertiesConstant.EXECUTOR_MAX_SIZE.getKey(), "10");
        shardingProperties = new ShardingProperties(prop);
    }
    
    @Test
    public void assertGetValueForDefaultValue() {
        ShardingProperties shardingProperties = new ShardingProperties(new Properties());
        boolean actualMetricsEnabled = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_ENABLE);
        long actualMetricsMillisecondsPeriod = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD);
        String actualMetricsPackageName = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_LOGGER_NAME);
        assertThat(actualMetricsEnabled, is(Boolean.valueOf(ShardingPropertiesConstant.METRICS_ENABLE.getDefaultValue())));
        assertThat(actualMetricsMillisecondsPeriod, is(Long.valueOf(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD.getDefaultValue())));
        assertThat(actualMetricsPackageName, is(ShardingPropertiesConstant.METRICS_LOGGER_NAME.getDefaultValue()));
        int executorMaxSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_MAX_SIZE);
        assertThat(executorMaxSize, is(Integer.valueOf(ShardingPropertiesConstant.EXECUTOR_MAX_SIZE.getDefaultValue())));
    }
    
    @Test
    public void assertGetValueForBoolean() {
        boolean actualMetricsEnabled = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_ENABLE);
        assertTrue(actualMetricsEnabled);
    }
    
    @Test
    public void assertGetValueForInteger() {
        int actualExecutorMaxSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_MAX_SIZE);
        assertThat(actualExecutorMaxSize, is(10));
    }
    
    @Test
    public void assertGetValueForLong() {
        long actualMetricsMillisecondsPeriod = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD);
        assertThat(actualMetricsMillisecondsPeriod, is(1000L));
    }
    
    @Test
    public void assertGetValueForString() {
        String actualMetricsPackageName = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_LOGGER_NAME);
        assertThat(actualMetricsPackageName, is("example"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValidateFailure() {
        Properties prop = new Properties();
        prop.put(ShardingPropertiesConstant.METRICS_ENABLE.getKey(), "error");
        prop.put(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD.getKey(), "error");
        prop.put(ShardingPropertiesConstant.EXECUTOR_MAX_SIZE.getKey(), "error");
        prop.put("other", "other");
        new ShardingProperties(prop);
    }
}
