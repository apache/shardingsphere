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

package org.apache.shardingsphere.transaction.xa.bitronix.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.resource.common.XAResourceHolder;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BitronixRecoveryResourceTest {
    
    @Mock
    private SingleXAResource singleXAResource;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private XAConnection xaConnection;
    
    @Mock
    private XAResource xaResource;
    
    private BitronixRecoveryResource bitronixRecoveryResource;
    
    @SneakyThrows
    @Before
    public void setUp() {
        when(singleXAResource.getResourceName()).thenReturn("ds1");
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        bitronixRecoveryResource = new BitronixRecoveryResource("ds1", xaDataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertRecovery() {
        XAResourceHolderState xaResourceHolderState = bitronixRecoveryResource.startRecovery();
        assertEquals(xaResourceHolderState.getUniqueName(), "ds1");
        XAResourceHolder xaResourceHolder = xaResourceHolderState.getXAResourceHolder();
        assertThat(xaResourceHolder.getXAResource(), is(xaResource));
        bitronixRecoveryResource.endRecovery();
        verify(xaConnection).close();
    }
    
    @SneakyThrows
    @Test
    public void assertFindXAResourceHolder() {
        assertNotNull(bitronixRecoveryResource.findXAResourceHolder(singleXAResource));
    }
}
