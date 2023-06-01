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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimestamp2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.TIMESTAMP2);
        when(payload.getByteBuf()).thenReturn(byteBuf);
    }
    
    @Test
    void assertReadWithoutFraction() {
        int currentSeconds = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        when(byteBuf.readInt()).thenReturn(currentSeconds);
        assertThat(new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload), is(new Timestamp(currentSeconds * 1000L)));
    }
    
    @Test
    void assertReadWithFraction() {
        columnDef.setColumnMeta(1);
        long currentTimeMillis = 1678795614082L;
        int currentSeconds = Long.valueOf(currentTimeMillis / 1000).intValue();
        int currentMilliseconds = Long.valueOf(currentTimeMillis % 100).intValue();
        when(payload.readInt1()).thenReturn(currentMilliseconds);
        when(byteBuf.readInt()).thenReturn(currentSeconds);
        assertThat("currentTimeMillis:" + currentTimeMillis, new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload), is(new Timestamp(currentSeconds * 1000L + currentMilliseconds * 10L)));
    }
    
    @Test
    void assertReadNullTime() {
        when(byteBuf.readInt()).thenReturn(0);
        assertThat(new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.DATETIME_OF_ZERO));
    }
}
