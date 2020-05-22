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

package org.apache.shardingsphere.metrics.spi;

import org.apache.shardingsphere.metrics.api.MetricsTrackerFactory;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.infra.spi.type.TypedSPI;

/**
 * Metrics tracker manager.
 */
public interface MetricsTrackerManager extends TypedSPI {
    
    /**
     * Start metrics tracker.
     *
     * @param metricsConfiguration metrics configuration
     */
    void start(MetricsConfiguration metricsConfiguration);
    
    /**
     * Gets metrics tracker factory.
     *
     * @return metrics tracker factory
     */
    MetricsTrackerFactory getMetricsTrackerFactory();
    
    /**
     * Stop metrics tracker.
     */
    void stop();
}

