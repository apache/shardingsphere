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

import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.resource.common.XAResourceHolder;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BitronixRecoveryResourceTest {
    
    @Mock
    private SingleXAResource singleXAResource;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private XAConnection xaConnection;
    
    @Mock
    private XAResource xaResource;
    
    private BitronixRecoveryResource bitronixRecoveryResource;
    
    @Before
    public void setUp() throws SQLException {
        when(singleXAResource.getResourceName()).thenReturn("ds1");
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        bitronixRecoveryResource = new BitronixRecoveryResource("ds1", xaDataSource);
    }
    
    @Test
    public void assertRecovery() throws SQLException {
        XAResourceHolderState xaResourceHolderState = bitronixRecoveryResource.startRecovery();
        assertThat(xaResourceHolderState.getUniqueName(), is("ds1"));
        XAResourceHolder xaResourceHolder = xaResourceHolderState.getXAResourceHolder();
        assertThat(xaResourceHolder.getXAResource(), is(xaResource));
        bitronixRecoveryResource.endRecovery();
        verify(xaConnection).close();
    }
    
    @Test
    public void assertFindXAResourceHolder() {
        assertNotNull(bitronixRecoveryResource.findXAResourceHolder(singleXAResource));
    }
}
