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

package org.apache.shardingsphere.shadow.algorithm.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Algorithm provided shadow rule configuration.
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class AlgorithmProvidedShadowRuleConfiguration implements SchemaRuleConfiguration {
    
    // fixme remove three fields when the api refactoring is complete
    private final String column;
    
    private final List<String> sourceDataSourceNames;
    
    private final List<String> shadowDataSourceNames;
    
    private Map<String, ShadowDataSourceConfiguration> dataSources = new LinkedHashMap<>();
    
    private Map<String, ShadowTableConfiguration> shadowTables = new LinkedHashMap<>();
    
    private Map<String, ShadowAlgorithm> shadowAlgorithms = new LinkedHashMap<>();
}
