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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.fixture.BinaryStatementRegistryUtil;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtExecutePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Before
    @After
    public void reset() {
        BinaryStatementRegistryUtil.reset();
    }
    
    @Test
    public void assertNewWithNotNullParameters() throws SQLException {
        MySQLBinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 0, 1);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload);
        assertThat(actual.getSequenceId(), is(0));
        assertThat(actual.getSql(), is("SELECT id FROM tbl WHERE id=?"));
        assertThat(actual.getParameters(), is(Collections.<Object>singletonList(1)));
    }
    
    @Test
    public void assertNewWithNullParameters() throws SQLException {
        MySQLBinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload);
        assertThat(actual.getSequenceId(), is(0));
        assertThat(actual.getSql(), is("SELECT id FROM tbl WHERE id=?"));
        assertThat(actual.getParameters(), is(Collections.singletonList(null)));
    }
    
    @Test
    public void assertWrite() throws SQLException {
        MySQLBinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload);
        actual.write(payload);
        verify(payload, times(2)).writeInt4(1);
        verify(payload, times(4)).writeInt1(1);
        verify(payload).writeInt1(0);
        verify(payload).writeStringLenenc("");
    }
}
