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

package org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.database.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.database.DatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.distsql.statement.rdl.rule.aware.StaticDataSourceContainedRuleAwareStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Drop database rule operator.
 */
@RequiredArgsConstructor
public final class DropDatabaseRuleOperator implements DatabaseRuleOperator {
    
    private final ContextManager contextManager;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleDropExecutor executor;
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<MetaDataVersion> operate(final RuleDefinitionStatement sqlStatement, final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return Collections.emptyList();
        }
        ModeContextManager modeContextManager = contextManager.getInstanceContext().getModeContextManager();
        RuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement, currentRuleConfig);
        if (sqlStatement instanceof StaticDataSourceContainedRuleAwareStatement) {
            database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)
                    .ifPresent(optional -> ((StaticDataSourceContainedRuleAwareStatement) sqlStatement).getNames().forEach(optional::cleanStorageNodeDataSource));
            // TODO refactor to new metadata refresh way
        }
        modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement, currentRuleConfig);
        return modeContextManager.alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
}
