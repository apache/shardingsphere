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

package org.apache.shardingsphere.broadcast.yaml.swapper;

import org.apache.shardingsphere.broadcast.constant.BroadcastOrder;
import org.apache.shardingsphere.broadcast.metadata.nodepath.BroadcastRuleNodePathProvider;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Broadcast rule configuration repository tuple swapper.
 */
public final class BroadcastRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<YamlBroadcastRuleConfiguration> {
    
    private final RuleNodePath ruleNodePath = new BroadcastRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlBroadcastRuleConfiguration yamlRuleConfig) {
        return yamlRuleConfig.getTables().isEmpty()
                ? Collections.emptyList()
                : Collections.singleton(new RepositoryTuple(BroadcastRuleNodePathProvider.TABLES, YamlEngine.marshal(yamlRuleConfig)));
    }
    
    @Override
    public Optional<YamlBroadcastRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        for (RepositoryTuple each : validTuples) {
            if (ruleNodePath.getRoot().isValidatedPath(each.getKey())) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), YamlBroadcastRuleConfiguration.class));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Class<YamlBroadcastRuleConfiguration> getTypeClass() {
        return YamlBroadcastRuleConfiguration.class;
    }
    
    @Override
    public int getOrder() {
        return BroadcastOrder.ORDER;
    }
    
    @Override
    public String getRuleTypeName() {
        return "broadcast";
    }
}
