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

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.annotation.ShardingTransactional;

public abstract class ShardingJDBCTransactionService extends CommonServiceImpl implements TransactionService {
    
    @Override
    @ShardingTransactional
    public void processFailureWithLocal() {
        printTransactionType();
        super.processFailure();
    }
    
    @Override
    @ShardingTransactional(type = TransactionType.XA)
    public void processFailureWithXa() {
        printTransactionType();
        super.processFailure();
    }
    
    @Override
    @ShardingTransactional(type = TransactionType.BASE)
    public void processFailureWithBase() {
        printTransactionType();
        super.processFailure();
    }
    
    @Override
    public void printTransactionType() {
        System.out.println(String.format("-------------- Process With Transaction %s ---------------", TransactionTypeHolder.get()));
    }
}
