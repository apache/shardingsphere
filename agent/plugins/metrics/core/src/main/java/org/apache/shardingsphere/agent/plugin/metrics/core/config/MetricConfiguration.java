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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Metric configuration.
 */
@RequiredArgsConstructor
@Getter
public final class MetricConfiguration {
    
    private final String id;
    
    private final MetricCollectorType type;
    
    private final String help;
    
    private final List<String> labels;
    
    private final Map<String, Object> props;
    
    public MetricConfiguration(final String id, final MetricCollectorType type, final String help) {
        this(id, type, help, Collections.emptyList(), Collections.emptyMap());
    }
    
    public MetricConfiguration(final String id, final MetricCollectorType type, final String help, final List<String> labels) {
        this(id, type, help, labels, Collections.emptyMap());
    }
    
    public MetricConfiguration(final String id, final MetricCollectorType type, final String help, final Map<String, Object> props) {
        this(id, type, help, Collections.emptyList(), props);
    }
}
