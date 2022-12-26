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

package org.apache.shardingsphere.single.yaml.swapper;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.api.config.rule.SingleTableRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.yaml.config.YamlSingleRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML single rule configuration swapper.
 */
public final class YamlSingleRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlSingleRuleConfiguration, SingleRuleConfiguration> {
    
    @Override
    public YamlSingleRuleConfiguration swapToYamlConfiguration(final SingleRuleConfiguration data) {
        YamlSingleRuleConfiguration result = new YamlSingleRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().add(swapToYamlConfiguration(each)));
        data.getDefaultDataSource().ifPresent(result::setDefaultDataSource);
        return result;
    }
    
    private String swapToYamlConfiguration(final SingleTableRuleConfiguration data) {
        return data.getTables().stream().map(each -> data.getDataSourceName() + "." + each.toString()).collect(Collectors.joining(","));
    }
    
    @Override
    public SingleRuleConfiguration swapToObject(final YamlSingleRuleConfiguration yamlConfig) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.setDefaultDataSource(yamlConfig.getDefaultDataSource());
        for (Entry<String, Collection<QualifiedTable>> entry : getDataSourceTables(yamlConfig.getTables()).entrySet()) {
            SingleTableRuleConfiguration singleTableRuleConfig = new SingleTableRuleConfiguration();
            singleTableRuleConfig.setDataSourceName(entry.getKey());
            singleTableRuleConfig.getTables().addAll(entry.getValue());
            result.getTables().add(singleTableRuleConfig);
        }
        return result;
    }
    
    private Map<String, Collection<QualifiedTable>> getDataSourceTables(final Collection<String> tableConfigs) {
        Map<String, Collection<QualifiedTable>> result = new HashMap<>(tableConfigs.size(), 1);
        for (String each : tableConfigs) {
            for (String qualifiedTableConfig : Splitter.on(",").trimResults().splitToList(each)) {
                List<String> segments = Splitter.on(".").splitToList(qualifiedTableConfig);
                Preconditions.checkArgument(segments.size() == 2 || segments.size() == 3, "Single table config must be two or three segments");
                Collection<QualifiedTable> qualifiedTables = result.computeIfAbsent(segments.get(0), key -> new LinkedList<>());
                qualifiedTables.add(segments.size() == 2 ? new QualifiedTable(null, segments.get(1)) : new QualifiedTable(segments.get(1), segments.get(2)));
            }
        }
        return result;
    }
    
    @Override
    public Class<SingleRuleConfiguration> getTypeClass() {
        return SingleRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SINGLE";
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
}
