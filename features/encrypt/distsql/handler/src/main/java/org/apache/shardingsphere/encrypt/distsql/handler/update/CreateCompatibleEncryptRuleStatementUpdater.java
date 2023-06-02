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

package org.apache.shardingsphere.encrypt.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

/**
 * Create encrypt rule statement updater.
 * 
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class CreateCompatibleEncryptRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateEncryptRuleStatement, CompatibleEncryptRuleConfiguration> {
    
    private final CreateEncryptRuleStatementUpdater delegate = new CreateEncryptRuleStatementUpdater();
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateEncryptRuleStatement sqlStatement, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        delegate.checkSQLStatement(database, sqlStatement, currentRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public CompatibleEncryptRuleConfiguration buildToBeCreatedRuleConfiguration(final CompatibleEncryptRuleConfiguration currentRuleConfig, final CreateEncryptRuleStatement sqlStatement) {
        EncryptRuleConfiguration ruleConfig = delegate.buildToBeCreatedRuleConfiguration(currentRuleConfig.convertToEncryptRuleConfiguration(), sqlStatement);
        return new CompatibleEncryptRuleConfiguration(ruleConfig.getTables(), ruleConfig.getEncryptors());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final CompatibleEncryptRuleConfiguration currentRuleConfig, final CompatibleEncryptRuleConfiguration toBeCreatedRuleConfig) {
        delegate.updateCurrentRuleConfiguration(currentRuleConfig.convertToEncryptRuleConfiguration(), toBeCreatedRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getRuleConfigurationClass() {
        return CompatibleEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateEncryptRuleStatement.class.getName();
    }
}
