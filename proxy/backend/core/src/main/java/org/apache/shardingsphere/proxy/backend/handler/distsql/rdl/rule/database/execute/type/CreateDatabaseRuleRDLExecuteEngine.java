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
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLCreateExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.DatabaseRuleRDLExecuteEngine;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Create database rule RDL execute engine.
 */
@RequiredArgsConstructor
public final class CreateDatabaseRuleRDLExecuteEngine implements DatabaseRuleRDLExecuteEngine {
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleRDLCreateExecutor executor;
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<MetaDataVersion> execute(final RuleDefinitionStatement sqlStatement, final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        ModeContextManager modeContextManager = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager();
        return modeContextManager.alterRuleConfiguration(database.getName(), getToBeCreatedRuleConfiguration(sqlStatement, database, currentRuleConfig, toBeCreatedRuleConfig));
    }
    
    private RuleConfiguration getToBeCreatedRuleConfiguration(final RuleDefinitionStatement sqlStatement,
                                                              final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig, final RuleConfiguration toBeCreatedRuleConfig) {
        if (sqlStatement instanceof LoadSingleTableStatement || sqlStatement instanceof SetDefaultSingleTableStorageUnitStatement) {
            return null == currentRuleConfig ? decorateRuleConfiguration(database, toBeCreatedRuleConfig) : decorateRuleConfiguration(database, currentRuleConfig);
        }
        return toBeCreatedRuleConfig;
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration decorateRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        return TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass()).map(optional -> optional.decorate(database.getName(),
                database.getResourceMetaData().getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules(), ruleConfig)).orElse(ruleConfig);
    }
}
