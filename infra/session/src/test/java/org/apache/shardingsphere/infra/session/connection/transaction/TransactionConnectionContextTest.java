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

package org.apache.shardingsphere.infra.session.connection.transaction;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TransactionConnectionContextTest {
    
    private final TransactionConnectionContext transactionConnectionContext = new TransactionConnectionContext();
    
    @Test
    void assertBeginTransaction() {
        transactionConnectionContext.beginTransaction("XA", mock(TransactionManager.class));
        assertThat(transactionConnectionContext.getTransactionType(), is(Optional.of("XA")));
        assertTrue(transactionConnectionContext.isInTransaction());
    }
    
    @Test
    void assertIsNotInDistributedTransactionWhenNotBegin() {
        assertFalse(transactionConnectionContext.isDistributedTransactionStarted());
    }
    
    @Test
    void assertIsNotInDistributedTransactionWithLocal() {
        transactionConnectionContext.beginTransaction("LOCAL", mock(TransactionManager.class));
        assertFalse(transactionConnectionContext.isDistributedTransactionStarted());
    }
    
    @Test
    void assertIsInDistributedTransactionWithXA() {
        transactionConnectionContext.beginTransaction("XA", mock(TransactionManager.class));
        assertTrue(transactionConnectionContext.isDistributedTransactionStarted());
    }
    
    @Test
    void assertIsInDistributedTransactionWithBASE() {
        transactionConnectionContext.beginTransaction("BASE", mock(TransactionManager.class));
        assertTrue(transactionConnectionContext.isDistributedTransactionStarted());
    }
    
    @Test
    void assertGetReadWriteSplitReplicaRoute() {
        transactionConnectionContext.setReadWriteSplitReplicaRoute("foo");
        assertThat(transactionConnectionContext.getReadWriteSplitReplicaRoute(), is(Optional.of("foo")));
    }
    
    @Test
    void assertClose() {
        transactionConnectionContext.beginTransaction("XA", mock(TransactionManager.class));
        transactionConnectionContext.close();
        assertFalse(transactionConnectionContext.getTransactionType().isPresent());
        assertFalse(transactionConnectionContext.isInTransaction());
        assertThat(transactionConnectionContext.getBeginMillis(), is(0L));
        assertFalse(transactionConnectionContext.isExceptionOccur());
        assertFalse(transactionConnectionContext.getReadWriteSplitReplicaRoute().isPresent());
    }
}
