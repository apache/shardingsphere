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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.infra.config.rule.checker.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.infra.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shadow import rule configuration provider.
 */
public final class ShadowImportRuleConfigurationProvider implements ImportRuleConfigurationProvider<ShadowRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShadowRuleConfiguration ruleConfig) {
        checkTables(databaseName, ruleConfig);
        checkShadowAlgorithms(ruleConfig);
    }
    
    private void checkTables(final String databaseName, final ShadowRuleConfiguration ruleConfig) {
        Collection<String> tableNames = ruleConfig.getTables().keySet();
        Collection<String> duplicatedTables = tableNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedTables.isEmpty(), () -> new DuplicateRuleException("SHADOW", databaseName, duplicatedTables));
    }
    
    private void checkShadowAlgorithms(final ShadowRuleConfiguration ruleConfig) {
        ruleConfig.getShadowAlgorithms().values().forEach(each -> TypedSPILoader.checkService(ShadowAlgorithm.class, each.getType(), each.getProps()));
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ShadowRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        ruleConfig.getDataSources().forEach(each -> {
            if (null != each.getShadowDataSourceName()) {
                result.add(each.getShadowDataSourceName());
            }
            if (null != each.getProductionDataSourceName()) {
                result.add(each.getProductionDataSourceName());
            }
        });
        return result;
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getType() {
        return ShadowRuleConfiguration.class;
    }
}
