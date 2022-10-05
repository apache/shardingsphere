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

package org.apache.shardingsphere.transaction.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.parser.statement.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Collection;

/**
 * Alter transaction rule statement handler.
 */
public final class AlterTransactionRuleStatementUpdater implements GlobalRuleRALUpdater {
    
    @Override
    public void executeUpdate(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Collection<ShardingSphereRule> globalRules = metaData.getGlobalRuleMetaData().getRules();
        globalRules.stream().filter(each -> each instanceof TransactionRule).forEach(each -> ((TransactionRule) each).closeStaleResource());
        globalRules.removeIf(each -> each instanceof TransactionRule);
        TransactionRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration(sqlStatement);
        globalRules.add(new TransactionRule(toBeAlteredRuleConfig, metaData.getDatabases(), null));
    }
    
    private TransactionRuleConfiguration createToBeAlteredRuleConfiguration(final SQLStatement sqlStatement) {
        AlterTransactionRuleStatement ruleStatement = (AlterTransactionRuleStatement) sqlStatement;
        return new TransactionRuleConfiguration(ruleStatement.getDefaultType(), ruleStatement.getProvider().getProviderType(), ruleStatement.getProvider().getProps());
    }
    
    @Override
    public String getType() {
        return AlterTransactionRuleStatement.class.getName();
    }
}
