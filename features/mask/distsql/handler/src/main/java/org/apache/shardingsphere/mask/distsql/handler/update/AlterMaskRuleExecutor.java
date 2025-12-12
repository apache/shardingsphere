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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.handler.converter.MaskRuleStatementConverter;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter mask rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(MaskRule.class)
@Setter
public final class AlterMaskRuleExecutor implements DatabaseRuleAlterExecutor<AlterMaskRuleStatement, MaskRule, MaskRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private MaskRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterMaskRuleStatement sqlStatement) {
        checkToBeAlteredRules(sqlStatement);
    }
    
    private void checkToBeAlteredRules(final AlterMaskRuleStatement sqlStatement) {
        Collection<String> currentMaskTableNames = rule.getConfiguration().getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedMaskTableNames = getToBeAlteredMaskTableNames(sqlStatement).stream().filter(each -> !currentMaskTableNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedMaskTableNames, () -> new MissingRequiredRuleException("Mask", database.getName(), notExistedMaskTableNames));
    }
    
    private Collection<String> getToBeAlteredMaskTableNames(final AlterMaskRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(MaskRuleSegment::getTableName).collect(Collectors.toList());
    }
    
    @Override
    public MaskRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterMaskRuleStatement sqlStatement) {
        return MaskRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public MaskRuleConfiguration buildToBeDroppedRuleConfiguration(final MaskRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> toBeAlteredTableNames = toBeAlteredRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<MaskColumnRuleConfiguration> columns = rule.getConfiguration().getTables().stream().filter(each -> !toBeAlteredTableNames.contains(each.getName()))
                .flatMap(each -> each.getColumns().stream()).collect(Collectors.toList());
        columns.addAll(toBeAlteredRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream()).collect(Collectors.toList()));
        Collection<String> inUsedAlgorithmNames = columns.stream().map(MaskColumnRuleConfiguration::getMaskAlgorithm).collect(Collectors.toSet());
        Map<String, AlgorithmConfiguration> toBeDroppedAlgorithms = new HashMap<>(rule.getConfiguration().getMaskAlgorithms().size(), 1F);
        for (String each : rule.getConfiguration().getMaskAlgorithms().keySet()) {
            if (!inUsedAlgorithmNames.contains(each)) {
                toBeDroppedAlgorithms.put(each, rule.getConfiguration().getMaskAlgorithms().get(each));
            }
        }
        return new MaskRuleConfiguration(Collections.emptyList(), toBeDroppedAlgorithms);
    }
    
    @Override
    public Class<MaskRule> getRuleClass() {
        return MaskRule.class;
    }
    
    @Override
    public Class<AlterMaskRuleStatement> getType() {
        return AlterMaskRuleStatement.class;
    }
}
