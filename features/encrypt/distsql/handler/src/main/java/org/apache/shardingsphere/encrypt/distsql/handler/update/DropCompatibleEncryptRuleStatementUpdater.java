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

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Drop encrypt rule statement updater.
 * 
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class DropCompatibleEncryptRuleStatementUpdater implements RuleDefinitionDropUpdater<DropEncryptRuleStatement, CompatibleEncryptRuleConfiguration> {
    
    private final DropEncryptRuleStatementUpdater delegate = new DropEncryptRuleStatementUpdater();
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropEncryptRuleStatement sqlStatement, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        delegate.checkSQLStatement(database, sqlStatement, currentRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropEncryptRuleStatement sqlStatement, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        return delegate.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public CompatibleEncryptRuleConfiguration buildToBeDroppedRuleConfiguration(final CompatibleEncryptRuleConfiguration currentRuleConfig, final DropEncryptRuleStatement sqlStatement) {
        Collection<EncryptTableRuleConfiguration> toBeDroppedTables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> toBeDroppedEncryptors = new HashMap<>();
        for (String each : sqlStatement.getTables()) {
            toBeDroppedTables.add(new EncryptTableRuleConfiguration(each, Collections.emptyList()));
        }
        // TODO find unused encryptor
        return new CompatibleEncryptRuleConfiguration(toBeDroppedTables, toBeDroppedEncryptors);
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropEncryptRuleStatement sqlStatement, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        return delegate.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig.convertToEncryptRuleConfiguration());
    }
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getRuleConfigurationClass() {
        return CompatibleEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropEncryptRuleStatement.class.getName();
    }
}
