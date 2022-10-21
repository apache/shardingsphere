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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * Create default single table rule statement updater.
 */
public final class CreateDefaultSingleTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultSingleTableRuleStatement, SingleTableRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateDefaultSingleTableRuleStatement sqlStatement, final SingleTableRuleConfiguration currentRuleConfig) {
        checkResourceExist(database, sqlStatement);
    }
    
    private void checkResourceExist(final ShardingSphereDatabase database, final CreateDefaultSingleTableRuleStatement sqlStatement) {
        if (StringUtils.isNotBlank(sqlStatement.getDefaultResource())) {
            Collection<String> resourceNames = database.getResourceMetaData().getDataSources().keySet();
            ShardingSpherePreconditions.checkState(resourceNames.contains(sqlStatement.getDefaultResource()),
                    () -> new MissingRequiredResourcesException(database.getName(), Collections.singleton(sqlStatement.getDefaultResource())));
        }
    }
    
    @Override
    public SingleTableRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultSingleTableRuleStatement sqlStatement) {
        SingleTableRuleConfiguration result = new SingleTableRuleConfiguration();
        result.setDefaultDataSource(sqlStatement.getDefaultResource());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final SingleTableRuleConfiguration currentRuleConfig, final SingleTableRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.setDefaultDataSource(toBeCreatedRuleConfig.getDefaultDataSource().orElse(null));
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getRuleConfigurationClass() {
        return SingleTableRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDefaultSingleTableRuleStatement.class.getName();
    }
}
