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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLTimestamp2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @Before
    public void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_TIMESTAMP2);
        when(payload.getByteBuf()).thenReturn(byteBuf);
    }
    
    @Test
    public void assertReadWithoutFraction() {
        int currentSeconds = new Long(System.currentTimeMillis() / 1000).intValue();
        when(byteBuf.readInt()).thenReturn(currentSeconds);
        assertThat(new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtil.getSimpleDateFormat().format(new Timestamp(currentSeconds * 1000L))));
    }
    
    @Test
    public void assertReadWithFraction() {
        columnDef.setColumnMeta(1);
        long currentTimeMillis = System.currentTimeMillis();
        int currentSeconds = new Long(currentTimeMillis / 1000).intValue();
        int currentMilliseconds = new Long(currentTimeMillis % 10).intValue();
        when(payload.readInt1()).thenReturn(currentMilliseconds);
        when(byteBuf.readInt()).thenReturn(currentSeconds);
        assertThat(new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload),
                   is(MySQLTimeValueUtil.getSimpleDateFormat().format(new Timestamp(currentSeconds * 1000L)) + "." + currentMilliseconds));
    }
    
    @Test
    public void assertReadNullTime() {
        when(byteBuf.readInt()).thenReturn(0);
        assertThat(new MySQLTimestamp2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtil.DATETIME_OF_ZERO));
    }
}
