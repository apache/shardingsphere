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

package io.shardingsphere.transaction.xa.jta;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShardingXAResourceTest {
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private Xid xid;
    
    private ShardingXAResource shardingXAResource;
    
    @Before
    public void setUp() {
        shardingXAResource = new ShardingXAResource("ds1", xaResource);
    }
    
    @Test
    public void assertCommit() throws XAException {
        shardingXAResource.commit(xid, true);
        verify(xaResource).commit(xid, true);
    }
    
    @Test
    public void assertEnd() throws XAException {
        shardingXAResource.end(xid, 1);
        verify(xaResource).end(xid, 1);
    }
    
    @Test
    public void assertForget() throws XAException {
        shardingXAResource.forget(xid);
        verify(xaResource).forget(xid);
    }
    
    @Test
    public void assertGetTransactionTimeout() throws XAException {
        shardingXAResource.getTransactionTimeout();
        verify(xaResource).getTransactionTimeout();
    }
    
    @Test
    public void assertIsSameRM() {
        assertTrue(shardingXAResource.isSameRM(new ShardingXAResource("ds1", xaResource)));
    }
    
    @Test
    public void assertPrepare() throws XAException {
        shardingXAResource.prepare(xid);
        verify(xaResource).prepare(xid);
    }
    
    @Test
    public void assertRecover() throws XAException {
        shardingXAResource.recover(1);
        verify(xaResource).recover(1);
    }
    
    @Test
    public void assertRollback() throws XAException {
        shardingXAResource.rollback(xid);
        verify(xaResource).rollback(xid);
    }
    
    @Test
    public void assertSetTransactionTimeout() throws XAException {
        shardingXAResource.setTransactionTimeout(1);
        verify(xaResource).setTransactionTimeout(1);
    }
    
    @Test
    public void assertStart() throws XAException {
        shardingXAResource.start(xid, 1);
        verify(xaResource).start(xid, 1);
    }
}
