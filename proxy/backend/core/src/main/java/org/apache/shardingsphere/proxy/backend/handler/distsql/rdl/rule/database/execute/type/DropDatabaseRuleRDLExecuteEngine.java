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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.database.DatabaseRuleRDLDropExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.DatabaseRuleRDLExecuteEngine;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.update.DropReadwriteSplittingRuleExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Drop database rule RDL execute engine.
 */
@RequiredArgsConstructor
public final class DropDatabaseRuleRDLExecuteEngine implements DatabaseRuleRDLExecuteEngine {
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleRDLDropExecutor executor;
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<MetaDataVersion> execute(final RuleDefinitionStatement sqlStatement, final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return Collections.emptyList();
        }
        ModeContextManager modeContextManager = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager();
        RuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(currentRuleConfig, sqlStatement);
        // TODO remove updateCurrentRuleConfiguration after update refactor completed.
        if (executor.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig) && ((DatabaseRuleConfiguration) currentRuleConfig).isEmpty()) {
            modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
            new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(currentRuleConfig)).values().stream().findFirst()
                    .ifPresent(optional -> modeContextManager.removeRuleConfiguration(database.getName(), optional.getRuleTagName().toLowerCase()));
            return Collections.emptyList();
        }
        if (executor instanceof DropReadwriteSplittingRuleExecutor) {
            database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)
                    .ifPresent(optional -> ((DropReadwriteSplittingRuleStatement) sqlStatement).getNames().forEach(optional::cleanStorageNodeDataSource));
            // TODO refactor to new metadata refresh way
        }
        modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(currentRuleConfig, sqlStatement);
        return modeContextManager.alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
}
