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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.metadata.converter.ReadwriteSplittingNodeConverter;

import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * TODO Rename YamlReadwriteSplittingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML readwrite-splitting rule configuration swapper.
 */
public final class NewYamlReadwriteSplittingRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ReadwriteSplittingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : data.getDataSources()) {
            result.add(new YamlDataNode(ReadwriteSplittingNodeConverter.getGroupNamePath(each.getName()), YamlEngine.marshal(each)));
        }
        for (Entry<String, AlgorithmConfiguration> entry : data.getLoadBalancers().entrySet()) {
            result.add(new YamlDataNode(ReadwriteSplittingNodeConverter.getLoadBalancerPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        return result;
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        // TODO
        return new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READWRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
}
