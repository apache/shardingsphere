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

package org.apache.shardingsphere.metrics.facade.fixture;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.metrics.api.MetricsTrackerFactory;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

import java.util.Properties;

@Getter
@Setter
public final class FirstMetricsTrackerManagerFixture implements MetricsTrackerManager {
    
    private final MetricsTrackerFactory metricsTrackerFactory = new FirstMetricsTrackerFactoryFixture();
    
    private Properties props = new Properties();
    
    @Override
    public void start(final MetricsConfiguration metricsConfiguration) {
    }
    
    @Override
    public void stop() {
    }
    
    @Override
    public String getType() {
        return "fixture";
    }
}

