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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimeBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogColumnDef columnDef;
    
    @Test
    void assertRead() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(16 * 10000 + 59 * 100 + 59);
        assertThat(new MySQLTimeBinlogProtocolValue().read(columnDef, payload), is("16:59:59"));
    }
    
    @Test
    void assertReadNullTime() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(0);
        assertThat(new MySQLTimeBinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.ZERO_OF_TIME));
    }
}
