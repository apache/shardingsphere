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

package org.apache.shardingsphere.mask.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Mask import rule configuration provider.
 */
public final class MaskImportRuleConfigurationProvider implements ImportRuleConfigurationProvider<MaskRuleConfiguration> {
    
    @Override
    public void check(final ShardingSphereDatabase database, final MaskRuleConfiguration ruleConfig) {
        if (null == database || null == ruleConfig) {
            return;
        }
        checkTables(ruleConfig, database.getName());
        checkMaskAlgorithms(ruleConfig);
        checkMaskAlgorithmsExisted(ruleConfig, database.getName());
    }
    
    private void checkTables(final MaskRuleConfiguration currentRuleConfig, final String databaseName) {
        Collection<String> tableNames = currentRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> duplicatedTables = tableNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedTables.isEmpty(), () -> new DuplicateRuleException("MASK", databaseName, duplicatedTables));
    }
    
    private void checkMaskAlgorithms(final MaskRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getMaskAlgorithms().values().forEach(each -> TypedSPILoader.checkService(MaskAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkMaskAlgorithmsExisted(final MaskRuleConfiguration currentRuleConfig, final String databaseName) {
        Collection<String> notExistedAlgorithms = currentRuleConfig.getTables().stream()
                .flatMap(each -> each.getColumns().stream()).map(MaskColumnRuleConfiguration::getMaskAlgorithm).collect(Collectors.toList());
        notExistedAlgorithms.removeIf(currentRuleConfig.getMaskAlgorithms().keySet()::contains);
        ShardingSpherePreconditions.checkState(notExistedAlgorithms.isEmpty(), () -> new MissingRequiredAlgorithmException(databaseName, notExistedAlgorithms));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getType() {
        return MaskRuleConfiguration.class;
    }
}
