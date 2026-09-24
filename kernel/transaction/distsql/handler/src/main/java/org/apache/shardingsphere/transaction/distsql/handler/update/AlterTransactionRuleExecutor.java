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

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.statement.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

/**
 * Alter transaction rule executor.
 */
public final class AlterTransactionRuleExecutor implements GlobalRuleDefinitionExecutor<AlterTransactionRuleStatement, TransactionRule> {
    
    @Override
    public TransactionRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterTransactionRuleStatement sqlStatement) {
        return new TransactionRuleConfiguration(sqlStatement.getDefaultType(), sqlStatement.getProvider().getProviderType(), sqlStatement.getProvider().getProps());
    }
    
    @Override
    public void setRule(final TransactionRule rule) {
    }
    
    @Override
    public Class<TransactionRule> getRuleClass() {
        return TransactionRule.class;
    }
    
    @Override
    public Class<AlterTransactionRuleStatement> getType() {
        return AlterTransactionRuleStatement.class;
    }
}
