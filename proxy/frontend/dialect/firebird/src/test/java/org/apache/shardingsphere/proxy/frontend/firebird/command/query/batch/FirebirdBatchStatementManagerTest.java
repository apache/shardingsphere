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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchStatementManagerTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 2;
    
    private final FirebirdBatchStatementManager manager = FirebirdBatchStatementManager.getInstance();
    
    @Mock
    private FirebirdBatchMessageCommandPacket packet;
    
    @BeforeEach
    void setUp() {
        manager.registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        manager.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertRegisterBatchStatement() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 16L, true, true);
        FirebirdBatchStatement actual = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actual.getStatementHandle(), is(STATEMENT_ID));
        assertThat(actual.getBufferSize(), is(16L));
        assertTrue(actual.isRecordCounts());
        assertTrue(actual.isMultiError());
    }
    
    @Test
    void assertAppendBatchMessage() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 8L, false, false);
        List<List<Object>> expectedParameterValues = Arrays.asList(Collections.singletonList("foo_value"), Collections.singletonList("bar_value"));
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(4);
        when(packet.readParameterValues(Collections.emptyList())).thenReturn(expectedParameterValues);
        manager.appendBatchMessage(CONNECTION_ID, packet);
        FirebirdBatchStatement actual = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actual.getParameterValues(), is(expectedParameterValues));
        assertThat(actual.getAccumulatedSize(), is(4L));
    }
    
    @Test
    void assertAppendBatchMessageWhenBatchNotFound() {
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        assertThrows(InvalidBatchHandleException.class, () -> manager.appendBatchMessage(CONNECTION_ID, packet));
        verify(packet, never()).getDataLength();
        verify(packet, never()).readParameterValues(anyList());
    }
    
    @Test
    void assertAppendBatchMessageWhenBufferSizeExceeded() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 8L, false, false);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(9);
        assertThrows(BatchTooBigException.class, () -> manager.appendBatchMessage(CONNECTION_ID, packet));
        verify(packet, never()).readParameterValues(anyList());
        FirebirdBatchStatement actual = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actual.getParameterValues(), is(Collections.emptyList()));
        assertThat(actual.getAccumulatedSize(), is(0L));
    }
    
    @Test
    void assertResetBatchStatement() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 8L, false, false);
        FirebirdBatchStatement batchStatement = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        batchStatement.addParameterValues(Collections.singletonList("foo_value"));
        batchStatement.addSize(4L);
        manager.resetBatchStatement(batchStatement);
        assertThat(batchStatement.getParameterValues(), is(Collections.emptyList()));
        assertThat(batchStatement.getAccumulatedSize(), is(0L));
    }
    
    @Test
    void assertUnregisterBatchStatement() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 8L, false, false);
        manager.unregisterBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertNull(manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertUnregisterBatchStatementWhenBatchNotFound() {
        assertDoesNotThrow(() -> manager.unregisterBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertUnregisterConnection() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, Collections.emptyList(), 8L, false, false);
        manager.unregisterConnection(CONNECTION_ID);
        assertNull(manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
}
