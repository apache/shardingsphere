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

package io.shardingsphere.example.repository.api.senario;

import io.shardingsphere.example.repository.api.service.TransactionService;
import org.apache.shardingsphere.transaction.core.TransactionType;

public class TransactionServiceScenario implements Scenario {
    
    private final TransactionService transactionService;
    
    public TransactionServiceScenario(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    public TransactionService getTransactionService() {
        return transactionService;
    }
    
    @Override
    public void executeShardingCRUDSuccess() {
        transactionService.initEnvironment();
        transactionService.processSuccess();
        transactionService.cleanEnvironment();
    }
    
    @Override
    public void executeShardingCRUDFailure() {
        transactionService.initEnvironment();
        processFailure(transactionService, TransactionType.LOCAL);
        processFailure(transactionService, TransactionType.XA);
        processFailure(transactionService, TransactionType.BASE);
        processFailure(transactionService, TransactionType.LOCAL);
        transactionService.cleanEnvironment();
    }
    
    private void processFailure(final TransactionService transactionService, final TransactionType type) {
        try {
            switch (type) {
                case LOCAL:
                    transactionService.processFailureWithLocal();
                    break;
                case XA:
                    transactionService.processFailureWithXa();
                    break;
                case BASE:
                    transactionService.processFailureWithBase();
                    break;
                default:
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            transactionService.printData();
        }
    }
}
