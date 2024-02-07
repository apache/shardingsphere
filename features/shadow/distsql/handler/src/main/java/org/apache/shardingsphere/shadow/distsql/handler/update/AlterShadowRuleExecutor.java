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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Map;

/**
 * Alter shadow rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShadowRule.class)
@Setter
public final class AlterShadowRuleExecutor implements DatabaseRuleAlterExecutor<AlterShadowRuleStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterShadowRuleStatement sqlStatement) {
        checkRuleNames(sqlStatement.getRules());
        ShadowRuleStatementChecker.checkStorageUnitsExist(ShadowRuleStatementSupporter.getStorageUnitNames(sqlStatement.getRules()), database);
        checkAlgorithms(sqlStatement.getRules());
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkRuleNames(final Collection<ShadowRuleSegment> segments) {
        Collection<String> currentRuleNames = ShadowRuleStatementSupporter.getRuleNames(rule.getConfiguration());
        Collection<String> requiredRuleNames = ShadowRuleStatementSupporter.getRuleNames(segments);
        ShadowRuleStatementChecker.checkDuplicated(requiredRuleNames, duplicated -> new DuplicateRuleException("shadow", database.getName(), duplicated));
        ShadowRuleStatementChecker.checkExisted(requiredRuleNames, currentRuleNames, notExistedRules -> new MissingRequiredRuleException("Shadow", notExistedRules));
    }
    
    private void checkAlgorithms(final Collection<ShadowRuleSegment> segments) {
        Collection<String> requiredAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(segments);
        ShadowRuleStatementChecker.checkDuplicated(requiredAlgorithms, duplicated -> new AlgorithmInUsedException("Shadow", database.getName(), duplicated));
    }
    
    private void checkAlgorithmType(final AlterShadowRuleStatement sqlStatement) {
        sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .map(ShadowAlgorithmSegment::getAlgorithmSegment).forEach(each -> TypedSPILoader.checkService(ShadowAlgorithm.class, each.getName(), each.getProps()));
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShadowRuleStatement sqlStatement) {
        return ShadowRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        UnusedAlgorithmFinder.findUnusedShadowAlgorithm(rule.getConfiguration()).forEach(each -> result.getShadowAlgorithms().put(each, rule.getConfiguration().getShadowAlgorithms().get(each)));
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        updateDataSources(currentRuleConfig, toBeAlteredRuleConfig.getDataSources());
        updateTables(currentRuleConfig.getTables(), toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
    }
    
    private void updateDataSources(final ShadowRuleConfiguration currentRuleConfig, final Collection<ShadowDataSourceConfiguration> toBeAlteredDataSources) {
        currentRuleConfig.getDataSources().addAll(toBeAlteredDataSources);
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeAlteredTables) {
        toBeAlteredTables.forEach(currentTables::replace);
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<AlterShadowRuleStatement> getType() {
        return AlterShadowRuleStatement.class;
    }
}
