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

package com.dangdang.ddframe.rdb.sharding.metrics;

import com.dangdang.ddframe.rdb.sharding.config.ShardingProperties;
import com.dangdang.ddframe.rdb.sharding.config.ShardingPropertiesConstant;
import org.junit.After;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class MetricsContextTest {
    
    @After
    public void tearDown() {
        MetricsContext.clear();
    }
    
    @Test
    public void assertStartWhenMetricsDisable() {
        initDisabledMetrics();
        assertNull(MetricsContext.start("name"));
    }
    
    @Test
    public void assertStartWhenMetricsEnable() {
        initEnabledMetrics();
        assertNotNull(MetricsContext.start("name"));
    }
    
    @Test
    public void assertStopWhenMetricsDisable() {
        initDisabledMetrics();
        MetricsContext.stop(null);
    }
    
    @Test
    public void assertStopWhenMetricsEnable() {
        initEnabledMetrics();
        MetricsContext.stop(MetricsContext.start("name"));
    }
    
    @Test
    public void assertClear() {
        initEnabledMetrics();
        MetricsContext.clear();
        assertNull(MetricsContext.start("name"));
    }
    
    private void initDisabledMetrics() {
        MetricsContext.init(new ShardingProperties(new Properties()));
    }
    
    private void initEnabledMetrics() {
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.METRICS_ENABLE.getKey(), Boolean.TRUE.toString());
        MetricsContext.init(new ShardingProperties(props));
    }
}
