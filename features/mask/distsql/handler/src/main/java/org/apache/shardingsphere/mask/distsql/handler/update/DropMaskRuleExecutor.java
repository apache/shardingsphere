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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
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
@DistSQLExecutorCurrentRuleRequired(MaskRule.class)
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
        Collection<String> currentMaskTableNames = rule.getConfiguration().getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !currentMaskTableNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedTableNames, () -> new MissingRequiredRuleException("Mask", database.getName(), notExistedTableNames));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropMaskRuleStatement sqlStatement) {
        return !Collections.disjoint(
                rule.getConfiguration().getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new)), sqlStatement.getTables());
    }
    
    @Override
    public MaskRuleConfiguration buildToBeDroppedRuleConfiguration(final DropMaskRuleStatement sqlStatement) {
        Collection<MaskTableRuleConfiguration> toBeDroppedTables = new LinkedList<>();
        for (String each : sqlStatement.getTables()) {
            toBeDroppedTables.add(new MaskTableRuleConfiguration(each, Collections.emptyList()));
            dropRule(each);
        }
        Map<String, AlgorithmConfiguration> toBeDroppedAlgorithms = new LinkedHashMap<>();
        findUnusedAlgorithms(rule.getConfiguration()).forEach(each -> toBeDroppedAlgorithms.put(each, rule.getConfiguration().getMaskAlgorithms().get(each)));
        return new MaskRuleConfiguration(toBeDroppedTables, toBeDroppedAlgorithms);
    }
    
    private void dropRule(final String ruleName) {
        Optional<MaskTableRuleConfiguration> maskTableRuleConfig = rule.getConfiguration().getTables().stream().filter(each -> each.getName().equalsIgnoreCase(ruleName)).findAny();
        maskTableRuleConfig.ifPresent(optional -> rule.getConfiguration().getTables().remove(maskTableRuleConfig.get()));
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
