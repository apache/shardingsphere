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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.integer;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLBitBinlogProtocolValueTest {
    
    @Mock
    private MySQLBinlogColumnDef columnDef;
    
    @Mock
    private MySQLPacketPayload payload;
    
    private MySQLBitBinlogProtocolValue actual;
    
    @Before
    public void setUp() {
        actual = new MySQLBitBinlogProtocolValue();
    }
    
    @Test
    public void assertReadWithLength1() {
        when(columnDef.getColumnMeta()).thenReturn(1);
        when(payload.readLong(1)).thenReturn(1L);
        assertThat(actual.read(columnDef, payload), is(1L));
    }
    
    @Test
    public void assertReadWithLength3() {
        when(columnDef.getColumnMeta()).thenReturn(516);
        when(payload.readLong(3)).thenReturn(1L);
        assertThat(actual.read(columnDef, payload), is(1L));
    }
    
    @Test
    public void assertReadWithLength5() {
        when(columnDef.getColumnMeta()).thenReturn(1280);
        when(payload.readLong(5)).thenReturn(1L);
        assertThat(actual.read(columnDef, payload), is(1L));
    }
    
    @Test
    public void assertReadWithLength8() {
        when(columnDef.getColumnMeta()).thenReturn(2048);
        when(payload.readLong(8)).thenReturn(-1L);
        assertThat(actual.read(columnDef, payload), is(-1L));
    }
}
