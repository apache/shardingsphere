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

package org.apache.shardingsphere.mask.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.statement.DropMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop mask rule statement executor.
 */
@DistSQLExecutorCurrentRuleRequired("Mask")
@Setter
public final class DropMaskRuleExecutor implements DatabaseRuleDropExecutor<DropMaskRuleStatement, MaskRule, MaskRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private MaskRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropMaskRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkToBeDroppedMaskTableNames(sqlStatement);
        }
    }
    
    private void checkToBeDroppedMaskTableNames(final DropMaskRuleStatement sqlStatement) {
        Collection<String> currentMaskTableNames = rule.getConfiguration().getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !currentMaskTableNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedTableNames.isEmpty(), () -> new MissingRequiredRuleException("Mask", database.getName(), notExistedTableNames));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig
                && !getIdenticalData(currentRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toSet()), sqlStatement.getTables()).isEmpty();
    }
    
    @Override
    public MaskRuleConfiguration buildToBeDroppedRuleConfiguration(final DropMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        Collection<MaskTableRuleConfiguration> toBeDroppedTables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> toBeDroppedAlgorithms = new LinkedHashMap<>();
        for (String each : sqlStatement.getTables()) {
            toBeDroppedTables.add(new MaskTableRuleConfiguration(each, Collections.emptyList()));
            dropRule(currentRuleConfig, each);
        }
        findUnusedAlgorithms(currentRuleConfig).forEach(each -> toBeDroppedAlgorithms.put(each, currentRuleConfig.getMaskAlgorithms().get(each)));
        return new MaskRuleConfiguration(toBeDroppedTables, toBeDroppedAlgorithms);
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        sqlStatement.getTables().forEach(each -> dropRule(currentRuleConfig, each));
        dropUnusedAlgorithm(currentRuleConfig);
        return currentRuleConfig.isEmpty();
    }
    
    private void dropRule(final MaskRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<MaskTableRuleConfiguration> maskTableRuleConfig = currentRuleConfig.getTables().stream().filter(each -> each.getName().equals(ruleName)).findAny();
        maskTableRuleConfig.ifPresent(optional -> currentRuleConfig.getTables().remove(maskTableRuleConfig.get()));
    }
    
    private void dropUnusedAlgorithm(final MaskRuleConfiguration currentRuleConfig) {
        findUnusedAlgorithms(currentRuleConfig).forEach(each -> currentRuleConfig.getMaskAlgorithms().remove(each));
    }
    
    private Collection<String> findUnusedAlgorithms(final MaskRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAlgorithms = currentRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream()).map(MaskColumnRuleConfiguration::getMaskAlgorithm)
                .collect(Collectors.toSet());
        return currentRuleConfig.getMaskAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
    
    @Override
    public Class<MaskRule> getRuleClass() {
        return MaskRule.class;
    }
    
    @Override
    public Class<DropMaskRuleStatement> getType() {
        return DropMaskRuleStatement.class;
    }
}
