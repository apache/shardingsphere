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

package org.apache.shardingsphere.transaction;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConnectionTransactionTest {
    
    private ConnectionTransaction connectionTransaction;
    
    @Before
    public void init() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(
                DefaultSchema.LOGIC_NAME,
                new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos")),
                transactionContexts
        );
    }
    
    @Test
    public void assertDistributedTransactionOperationTypeIgnore() {
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(false);
        assertThat(operationType, is(DistributedTransactionOperationType.IGNORE));
    }
}
