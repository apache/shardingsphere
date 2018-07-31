/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.common.config;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.transaction.api.TransactionManager;
import io.shardingsphere.transaction.common.TransactionContext;
import io.shardingsphere.transaction.common.TransactionContextFactory;
import io.shardingsphere.transaction.common.TransactionContextHolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JDBC transaction configuration.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JDBCTransactionConfiguration extends TransactionConfigurationAdapter {
    
    private static final JDBCTransactionConfiguration CONFIG = new JDBCTransactionConfiguration();
    
    /**
     * Get singleton instance of {@code JDBCTransactionConfiguration}.
     *
     * @return JDBC transaction configuration
     */
    public static JDBCTransactionConfiguration getInstance() {
        return CONFIG;
    }
    
    @Override
    protected TransactionManager doXaTransactionConfiguration(final TransactionType transactionType) {
        Optional<TransactionManager> transactionManager = doSPIConfiguration(transactionType);
        TransactionContext transactionContext = transactionManager.isPresent()
                ? TransactionContextFactory.newXAContext(transactionManager.get()) : TransactionContextFactory.newWeakXAContext();
        TransactionContextHolder.set(transactionContext);
        return transactionContext.getTransactionManager();
    }
}
