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

package io.shardingsphere.example.repository.api.service;

import io.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.springframework.transaction.annotation.Transactional;

public abstract class ShardingJDBCTransactionService extends CommonServiceImpl implements TransactionService {
    
    /**
     * process success with local.
     */
    @ShardingTransactionType
    @Transactional
    @Override
    public void processSuccessWithLocal() {
        printTransactionType();
        super.processSuccess();
    }
    
    /**
     * process success with XA.
     */
    @ShardingTransactionType(TransactionType.XA)
    @Transactional
    @Override
    public void processSuccessWithXA() {
        printTransactionType();
        super.processSuccess();
    }
    
    /**
     * process success with BASE.
     */
    @ShardingTransactionType(TransactionType.BASE)
    @Transactional
    @Override
    public void processSuccessWithBase() {
        printTransactionType();
        super.processSuccess();
    }
    
    /**
     * process failure with local.
     */
    @ShardingTransactionType
    @Transactional
    @Override
    public void processFailureWithLocal() {
        printTransactionType();
        super.processFailure();
    }
    
    /**
     * process failure with XA.
     */
    @ShardingTransactionType(TransactionType.XA)
    @Transactional
    @Override
    public void processFailureWithXA() {
        printTransactionType();
        super.processFailure();
    }
    
    /**
     * process failure with BASE.
     */
    @ShardingTransactionType(TransactionType.BASE)
    @Transactional
    @Override
    public void processFailureWithBase() {
        printTransactionType();
        super.processFailure();
    }
    
    @Override
    public final void printTransactionType() {
        System.out.println(String.format("-------------- Process With Transaction %s ---------------", TransactionTypeHolder.get()));
    }
}
