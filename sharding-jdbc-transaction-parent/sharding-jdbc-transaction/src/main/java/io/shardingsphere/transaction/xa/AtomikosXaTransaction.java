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

import io.shardingsphere.core.transaction.event.TransactionEvent;
import io.shardingsphere.core.transaction.spi.Transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Atomikos XA transaction implement for Transaction spi.
 *
 * @author zhaojun
 */
public class AtomikosXaTransaction implements Transaction {
    
    private static UserTransaction userTransaction = AtomikosUserTransaction.getInstance();
    
    @Override
    public void begin(TransactionEvent transactionEvent) throws SystemException, NotSupportedException {
        userTransaction.begin();
    }
    
    @Override
    public void commit(TransactionEvent transactionEvent) throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        userTransaction.commit();
    }
    
    @Override
    public void rollback(TransactionEvent transactionEvent) throws SystemException {
        userTransaction.rollback();
    }
}
