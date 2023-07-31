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

import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.ral.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.parser.statement.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import java.util.Collection;
import java.util.Optional;

/**
 * Alter transaction rule statement handler.
 */
public final class AlterTransactionRuleStatementUpdater implements GlobalRuleRALUpdater<AlterTransactionRuleStatement, TransactionRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final TransactionRuleConfiguration currentRuleConfig, final AlterTransactionRuleStatement sqlStatement) {
        checkTransactionType(sqlStatement);
        TransactionType transactionType = TransactionType.valueOf(sqlStatement.getDefaultType().toUpperCase());
        if (TransactionType.LOCAL == transactionType) {
            return;
        }
        checkTransactionManager(sqlStatement, transactionType);
        
    }
    
    private void checkTransactionType(final AlterTransactionRuleStatement statement) {
        try {
            TransactionType.valueOf(statement.getDefaultType().toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            throw new InvalidRuleConfigurationException("Transaction", String.format("Unsupported transaction type `%s`", statement.getDefaultType()));
        }
    }
    
    private void checkTransactionManager(final AlterTransactionRuleStatement statement, final TransactionType transactionType) {
        Collection<ShardingSphereTransactionManager> transactionManagers = ShardingSphereServiceLoader.getServiceInstances(ShardingSphereTransactionManager.class);
        Optional<ShardingSphereTransactionManager> transactionManager = transactionManagers.stream().filter(each -> transactionType == each.getTransactionType()).findFirst();
        ShardingSpherePreconditions.checkState(transactionManager.isPresent(),
                () -> new InvalidRuleConfigurationException("Transaction", String.format("No transaction manager with type `%s`", statement.getDefaultType())));
        if (TransactionType.XA == transactionType) {
            checkTransactionManagerProviderType(transactionManager.get(), statement.getProvider().getProviderType());
        }
    }
    
    private void checkTransactionManagerProviderType(final ShardingSphereTransactionManager transactionManager, final String providerType) {
        ShardingSpherePreconditions.checkState(transactionManager.containsProviderType(providerType),
                () -> new InvalidRuleConfigurationException("Transaction", String.format("No transaction manager provider with type `%s`", providerType)));
    }
    
    @Override
    public TransactionRuleConfiguration buildAlteredRuleConfiguration(final TransactionRuleConfiguration currentRuleConfig, final AlterTransactionRuleStatement sqlStatement) {
        return new TransactionRuleConfiguration(sqlStatement.getDefaultType(), sqlStatement.getProvider().getProviderType(), sqlStatement.getProvider().getProps());
    }
    
    @Override
    public Class<TransactionRuleConfiguration> getRuleConfigurationClass() {
        return TransactionRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterTransactionRuleStatement.class.getName();
    }
}
