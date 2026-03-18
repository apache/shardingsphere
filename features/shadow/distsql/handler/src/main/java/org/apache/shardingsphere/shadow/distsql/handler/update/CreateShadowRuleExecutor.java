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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;

/**
 * Create shadow rule executor.
 */
@Setter
public final class CreateShadowRuleExecutor implements DatabaseRuleCreateExecutor<CreateShadowRuleStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateShadowRuleStatement sqlStatement) {
        checkDuplicatedRules(sqlStatement);
        checkStorageUnits(sqlStatement.getRules());
        checkAlgorithms(sqlStatement.getRules());
        checkAlgorithmType(sqlStatement.getRules());
    }
    
    private void checkDuplicatedRules(final CreateShadowRuleStatement sqlStatement) {
        Collection<String> toBeCreatedRuleNames = ShadowRuleStatementSupporter.getRuleNames(sqlStatement.getRules());
        ShadowRuleStatementChecker.checkDuplicated(toBeCreatedRuleNames, duplicated -> new DuplicateRuleException("shadow", database.getName(), duplicated));
        ShadowRuleStatementChecker.checkDuplicatedWithLogicDataSource(toBeCreatedRuleNames, database);
        if (!sqlStatement.isIfNotExists()) {
            toBeCreatedRuleNames.retainAll(ShadowRuleStatementSupporter.getRuleNames(null == rule ? null : rule.getConfiguration()));
            ShardingSpherePreconditions.checkMustEmpty(toBeCreatedRuleNames, () -> new DuplicateRuleException("shadow", database.getName(), toBeCreatedRuleNames));
        }
    }
    
    private void checkStorageUnits(final Collection<ShadowRuleSegment> segments) {
        ShadowRuleStatementChecker.checkStorageUnitsExist(ShadowRuleStatementSupporter.getStorageUnitNames(segments), database);
    }
    
    private void checkAlgorithms(final Collection<ShadowRuleSegment> segments) {
        ShadowRuleStatementChecker.checkDuplicated(ShadowRuleStatementSupporter.getAlgorithmNames(segments), duplicated -> new DuplicateRuleException("shadow", database.getName(), duplicated));
    }
    
    private void checkAlgorithmType(final Collection<ShadowRuleSegment> segments) {
        segments.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .map(ShadowAlgorithmSegment::getAlgorithmSegment).forEach(each -> TypedSPILoader.checkService(ShadowAlgorithm.class, each.getName(), each.getProps()));
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShadowRuleStatement sqlStatement) {
        Collection<ShadowRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> toBeCreatedRuleNames = ShadowRuleStatementSupporter.getRuleNames(sqlStatement.getRules());
            toBeCreatedRuleNames.retainAll(ShadowRuleStatementSupporter.getRuleNames(rule.getConfiguration()));
            segments.removeIf(each -> toBeCreatedRuleNames.contains(each.getRuleName()));
        }
        return ShadowRuleStatementConverter.convert(segments);
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<CreateShadowRuleStatement> getType() {
        return CreateShadowRuleStatement.class;
    }
}
