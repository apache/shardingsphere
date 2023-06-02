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

package org.apache.shardingsphere.transaction.xa.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SingleXAResourceTest {
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private Xid xid;
    
    private SingleXAResource singleXAResource;
    
    @BeforeEach
    void setUp() {
        singleXAResource = new SingleXAResource("ds1", xaResource);
    }
    
    @Test
    void assertCommit() throws XAException {
        singleXAResource.commit(xid, true);
        verify(xaResource).commit(xid, true);
    }
    
    @Test
    void assertEnd() throws XAException {
        singleXAResource.end(xid, 1);
        verify(xaResource).end(xid, 1);
    }
    
    @Test
    void assertForget() throws XAException {
        singleXAResource.forget(xid);
        verify(xaResource).forget(xid);
    }
    
    @Test
    void assertGetTransactionTimeout() throws XAException {
        singleXAResource.getTransactionTimeout();
        verify(xaResource).getTransactionTimeout();
    }
    
    @Test
    void assertIsSameRM() {
        assertTrue(singleXAResource.isSameRM(new SingleXAResource("ds1", xaResource)));
    }
    
    @Test
    void assertPrepare() throws XAException {
        singleXAResource.prepare(xid);
        verify(xaResource).prepare(xid);
    }
    
    @Test
    void assertRecover() throws XAException {
        singleXAResource.recover(1);
        verify(xaResource).recover(1);
    }
    
    @Test
    void assertRollback() throws XAException {
        singleXAResource.rollback(xid);
        verify(xaResource).rollback(xid);
    }
    
    @Test
    void assertSetTransactionTimeout() throws XAException {
        singleXAResource.setTransactionTimeout(1);
        verify(xaResource).setTransactionTimeout(1);
    }
    
    @Test
    void assertStart() throws XAException {
        singleXAResource.start(xid, 1);
        verify(xaResource).start(xid, 1);
    }
}
