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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.KernelSQLException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Drop shadow algorithm executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShadowRule.class)
@Setter
public final class DropShadowAlgorithmExecutor implements DatabaseRuleDropExecutor<DropShadowAlgorithmStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShadowAlgorithmStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkAlgorithm(sqlStatement);
        }
    }
    
    private void checkAlgorithm(final DropShadowAlgorithmStatement sqlStatement) {
        Collection<String> currentAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(rule.getConfiguration());
        Collection<String> requiredAlgorithms = sqlStatement.getNames();
        String defaultShadowAlgorithmName = rule.getConfiguration().getDefaultShadowAlgorithmName();
        if (!sqlStatement.isIfExists()) {
            ShadowRuleStatementChecker.checkExisted(requiredAlgorithms, currentAlgorithms,
                    notExistedAlgorithms -> new UnregisteredAlgorithmException("Shadow", notExistedAlgorithms, new SQLExceptionIdentifier(database.getName())));
        }
        checkAlgorithmInUsed(requiredAlgorithms, getAlgorithmInUse(), identical -> new InUsedAlgorithmException("Shadow", database.getName(), identical));
        ShardingSpherePreconditions.checkNotContains(requiredAlgorithms, defaultShadowAlgorithmName,
                () -> new InUsedAlgorithmException("Shadow", database.getName(), Collections.singleton(defaultShadowAlgorithmName)));
    }
    
    private void checkAlgorithmInUsed(final Collection<String> requiredAlgorithms, final Collection<String> currentAlgorithms, final Function<Collection<String>, KernelSQLException> thrower) {
        ShadowRuleStatementChecker.checkDuplicated(requiredAlgorithms, currentAlgorithms, thrower);
    }
    
    private Collection<String> getAlgorithmInUse() {
        return rule.getConfiguration().getTables().values().stream().filter(each -> !each.getDataSourceNames().isEmpty()).map(ShadowTableConfiguration::getShadowAlgorithmNames)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShadowAlgorithmStatement sqlStatement) {
        return !Collections.disjoint(ShadowRuleStatementSupporter.getAlgorithmNames(rule.getConfiguration()), sqlStatement.getNames());
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShadowAlgorithmStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getShadowAlgorithms().put(each, rule.getConfiguration().getShadowAlgorithms().get(each));
        }
        return result;
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<DropShadowAlgorithmStatement> getType() {
        return DropShadowAlgorithmStatement.class;
    }
}
