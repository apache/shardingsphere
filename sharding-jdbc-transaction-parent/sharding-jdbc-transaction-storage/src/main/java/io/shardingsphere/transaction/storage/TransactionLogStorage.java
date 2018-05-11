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

package io.shardingsphere.transaction.storage;

import java.sql.Connection;
import java.util.List;

/**
 * Transaction log storage interface.
 * 
 * @author zhangliang
 */
public interface TransactionLogStorage {
    
    /**
     * Save transaction log.
     * 
     * @param transactionLog transaction log
     */
    void add(TransactionLog transactionLog);
    
    /**
     * Remove transaction log.
     * 
     * @param id transaction log id
     */
    void remove(String id);
    
    /**
     * Find eligible transaction logs.
     * 
     * <p>To be processed transaction logs: </p>
     * <p>1. retry times less than max retry times.</p>
     * <p>2. transaction log last retry timestamp interval early than last retry timestamp.</p>
     * 
     * @param size size of fetch transaction log
     * @param maxDeliveryTryTimes max delivery try times
     * @param maxDeliveryTryDelayMillis max delivery try delay millis
     * @return eligible transaction logs
     */
    List<TransactionLog> findEligibleTransactionLogs(int size, int maxDeliveryTryTimes, long maxDeliveryTryDelayMillis);
    
    /**
     * Increase asynchronized delivery try times.
     * 
     * @param id transaction log id
     */
    void increaseAsyncDeliveryTryTimes(String id);
    
    /**
     * Process transaction logs.
     *
     * @param connection connection for business app
     * @param transactionLog transaction log
     * @param maxDeliveryTryTimes max delivery try times
     * @return process success or not
     */
    boolean processData(Connection connection, TransactionLog transactionLog, int maxDeliveryTryTimes);
}
