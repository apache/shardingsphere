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

package org.apache.shardingsphere.agent.plugin.metrics.core.config;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;

/**
 * Metric configuration.
 */
@RequiredArgsConstructor
public final class MetricsConfiguration {
    
    private final Collection<MetricConfiguration> metrics;
    
    /**
     * Find metric configuration.
     * 
     * @param id metric ID
     * @return metric configuration
     */
    public Optional<MetricConfiguration> find(final String id) {
        return metrics.stream().filter(each -> id.equals(each.getId())).findFirst();
    }
}
