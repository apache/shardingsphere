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

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

/**
 * Alter encrypt rule statement updater.
 * 
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class AlterCompatibleEncryptRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterEncryptRuleStatement, CompatibleEncryptRuleConfiguration> {
    
    private final AlterEncryptRuleStatementUpdater delegate = new AlterEncryptRuleStatementUpdater();
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterEncryptRuleStatement sqlStatement, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        delegate.checkSQLStatement(database, sqlStatement, currentRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public CompatibleEncryptRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterEncryptRuleStatement sqlStatement) {
        EncryptRuleConfiguration ruleConfig = delegate.buildToBeAlteredRuleConfiguration(sqlStatement);
        return new CompatibleEncryptRuleConfiguration(ruleConfig.getTables(), ruleConfig.getEncryptors());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final CompatibleEncryptRuleConfiguration currentRuleConfig, final CompatibleEncryptRuleConfiguration toBeAlteredRuleConfig) {
        delegate.updateCurrentRuleConfiguration(currentRuleConfig.convertToEncryptRuleConfiguration(), toBeAlteredRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getRuleConfigurationClass() {
        return CompatibleEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterEncryptRuleStatement.class.getName();
    }
}
