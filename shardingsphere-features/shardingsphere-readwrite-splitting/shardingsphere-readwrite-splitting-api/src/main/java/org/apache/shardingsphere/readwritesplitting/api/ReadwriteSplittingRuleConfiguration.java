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

package org.apache.shardingsphere.readwritesplitting.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Readwrite-splitting rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ReadwriteSplittingRuleConfiguration implements SchemaRuleConfiguration, DistributedRuleConfiguration {
    
    private final Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources;
    
    private final Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers;
}
