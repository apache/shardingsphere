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

package org.apache.shardingsphere.globalclock.core.yaml.swapper;

import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.rule.constant.GlobalClockOrder;
import org.apache.shardingsphere.globalclock.core.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Global clock rule configuration repository tuple swapper.
 */
public final class GlobalClockRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<GlobalClockRuleConfiguration, YamlGlobalClockRuleConfiguration> {
    
    private final YamlGlobalClockRuleConfigurationSwapper ruleConfigSwapper = new YamlGlobalClockRuleConfigurationSwapper();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlGlobalClockRuleConfiguration yamlRuleConfig) {
        return Collections.singleton(new RepositoryTuple(getRuleTagName().toLowerCase(), YamlEngine.marshal(yamlRuleConfig)));
    }
    
    @Override
    public Optional<YamlGlobalClockRuleConfiguration> swapToObject0(final Collection<RepositoryTuple> repositoryTuples) {
        for (RepositoryTuple each : repositoryTuples) {
            if (GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey()).isPresent()) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), YamlGlobalClockRuleConfiguration.class));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<GlobalClockRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        for (RepositoryTuple each : repositoryTuples) {
            if (GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey()).isPresent()) {
                return Optional.of(ruleConfigSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlGlobalClockRuleConfiguration.class)));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public String getRuleTagName() {
        return "GLOBAL_CLOCK";
    }
    
    @Override
    public int getOrder() {
        return GlobalClockOrder.ORDER;
    }
    
    @Override
    public Class<GlobalClockRuleConfiguration> getTypeClass() {
        return GlobalClockRuleConfiguration.class;
    }
}
