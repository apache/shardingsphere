/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.integrate.storage;

import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.google.common.collect.Lists;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractTransactionLogStorageOperationsTest {
    
    protected void assertTransactionLogStorageOperations(final TransactionLogStorage storage) {
        String id = UUID.randomUUID().toString();
        String transactionId = UUID.randomUUID().toString();
        TransactionLog transactionLog = new TransactionLog(id, transactionId, SoftTransactionType.BestEffortsDelivery, 
            "ds_1", "UPDATE t_order_0 SET not_existed_column = 1 WHERE user_id = 1 AND order_id = ?", Lists.newArrayList(), 1461062858701L, 0);
        storage.add(transactionLog);
        storage.increaseAsyncDeliveryTryTimes(id);
        assertThat(storage.findEligibleTransactionLogs(1, 2, 0L).get(0).getAsyncDeliveryTryTimes(), is(1));
        storage.remove(id);
        assertThat(storage.findEligibleTransactionLogs(1, 2, 0L).size(), is(0));
    }
}
