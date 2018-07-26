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

package io.shardingsphere.transaction.xa;

import com.atomikos.icatch.jta.UserTransactionManager;
import io.shardingsphere.core.transaction.event.TransactionEvent;
import io.shardingsphere.core.transaction.event.XaTransactionEvent;
import io.shardingsphere.core.transaction.spi.TransactionEventHolder;
import io.shardingsphere.core.transaction.spi.TransactionManager;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * Atomikos XA transaction implement for Transaction SPI.
 *
 * @author zhaojun
 */
public class AtomikosXaTransaction implements TransactionManager {
    
    private static UserTransactionManager transactionManager = AtomikosUserTransaction.getInstance();
    
    static {
        try {
            init();
        } catch (SystemException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Init.
     *
     * @throws SystemException System Exception
     */
    private static void init() throws SystemException {
        TransactionEventHolder.set(XaTransactionEvent.class);
        transactionManager.init();
    }
    
    @Override
    public void begin(final TransactionEvent transactionEvent) throws SystemException, NotSupportedException {
        transactionManager.begin();
    }
    
    @Override
    public void commit(final TransactionEvent transactionEvent) throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        transactionManager.commit();
    }
    
    @Override
    public void rollback(final TransactionEvent transactionEvent) throws SystemException {
        transactionManager.rollback();
    }
}
