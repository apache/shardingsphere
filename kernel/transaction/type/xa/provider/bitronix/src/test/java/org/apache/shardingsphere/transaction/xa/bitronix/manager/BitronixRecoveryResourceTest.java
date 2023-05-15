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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitronixRecoveryResourceTest {
    
    @Mock
    private SingleXAResource singleXAResource;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private XAConnection xaConnection;
    
    @Mock
    private XAResource xaResource;
    
    private BitronixRecoveryResource bitronixRecoveryResource;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(singleXAResource.getResourceName()).thenReturn("ds1");
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        bitronixRecoveryResource = new BitronixRecoveryResource("ds1", xaDataSource);
    }
    
    @Test
    void assertRecovery() throws SQLException {
        XAResourceHolderState xaResourceHolderState = bitronixRecoveryResource.startRecovery();
        assertThat(xaResourceHolderState.getUniqueName(), is("ds1"));
        XAResourceHolder xaResourceHolder = xaResourceHolderState.getXAResourceHolder();
        assertThat(xaResourceHolder.getXAResource(), is(xaResource));
        bitronixRecoveryResource.endRecovery();
        verify(xaConnection).close();
    }
    
    @Test
    void assertRecoveryWhenXaConnectionIsNull() throws SQLException {
        BitronixRecoveryResource bitronixRecoveryResource = new BitronixRecoveryResource("ds1", null);
        bitronixRecoveryResource.endRecovery();
        verify(xaConnection, times(0)).close();
    }
    
    @Test
    void assertRecoveryWhenSQLExceptionIsThrown() throws SQLException {
        when(xaDataSource.getXAConnection()).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> bitronixRecoveryResource.startRecovery());
    }
    
    @Test
    void assertFindXAResourceHolder() {
        assertNotNull(bitronixRecoveryResource.findXAResourceHolder(singleXAResource));
    }
    
    @Test
    void assertCreatePooledConnection() {
        assertNull(bitronixRecoveryResource.createPooledConnection(null, null));
    }
    
    @Test
    void assertGetReference() {
        assertNull(bitronixRecoveryResource.getReference());
    }
    
    @Test
    void assertInit() {
        assertDoesNotThrow(() -> bitronixRecoveryResource.init());
    }
    
    @Test
    void assertSetFailed() {
        assertDoesNotThrow(() -> bitronixRecoveryResource.setFailed(true));
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(() -> bitronixRecoveryResource.close());
    }
}
