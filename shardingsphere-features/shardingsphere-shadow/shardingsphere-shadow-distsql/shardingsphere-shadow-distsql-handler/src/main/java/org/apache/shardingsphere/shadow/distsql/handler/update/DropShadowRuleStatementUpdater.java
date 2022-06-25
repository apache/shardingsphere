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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;

import java.util.Collection;

/**
 * Drop shadow rule statement updater.
 */
public final class DropShadowRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShadowRuleStatement, ShadowRuleConfiguration> {
    
    private static final String SHADOW = "shadow";
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isIfExists() && !isExistRuleConfig(currentRuleConfig)) {
            return;
        }
        checkConfigurationExist(database.getName(), currentRuleConfig);
        checkRuleNames(database.getName(), sqlStatement, currentRuleConfig);
    }
    
    private void checkConfigurationExist(final String databaseName, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        ShadowRuleStatementChecker.checkConfigurationExist(databaseName, currentRuleConfig);
    }
    
    private void checkRuleNames(final String databaseName, final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().keySet();
        if (!sqlStatement.isIfExists()) {
            ShadowRuleStatementChecker.checkRulesExist(sqlStatement.getRuleNames(), currentRuleNames, different -> new RequiredRuleMissedException(SHADOW, databaseName, different));
        }
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(sqlStatement.getRuleNames(), currentRuleConfig.getDataSources().keySet()).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> ruleNames = sqlStatement.getRuleNames();
        ruleNames.forEach(each -> currentRuleConfig.getDataSources().remove(each));
        currentRuleConfig.getTables().forEach((key, value) -> value.getDataSourceNames().removeIf(ruleNames::contains));
        return false;
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShadowRuleStatement.class.getName();
    }
}
