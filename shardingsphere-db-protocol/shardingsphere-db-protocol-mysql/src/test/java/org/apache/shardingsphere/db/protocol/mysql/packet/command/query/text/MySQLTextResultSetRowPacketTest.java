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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text;

import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLTextResultSetRowPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNew() {
        when(payload.readInt1()).thenReturn(1);
        when(payload.readStringLenenc()).thenReturn("value_a", null, "value_c");
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(payload, 3);
        assertThat(actual.getSequenceId(), is(1));
        verify(payload, times(3)).readStringLenenc();
    }
    
    @Test
    public void assertWrite() {
        long now = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(now);
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(1, Arrays.asList(null, "value", BigDecimal.ONE, new byte[] {}, timestamp));
        actual.write(payload);
        verify(payload).writeInt1(0xfb);
        verify(payload).writeStringLenenc("value");
        verify(payload).writeStringLenenc("1");
        if (0 == timestamp.getNanos()) {
            verify(payload).writeStringLenenc(timestamp.toString().split("\\.")[0]);
        } else {
            verify(payload).writeStringLenenc(timestamp.toString());
        }
    }
    
    @Test
    public void assertTimestampWithoutNanos() {
        long now = System.currentTimeMillis() / 1000 * 1000;
        Timestamp timestamp = new Timestamp(now);
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(1, Arrays.asList(null, "value", BigDecimal.ONE, new byte[]{}, timestamp));
        actual.write(payload);
        verify(payload).writeInt1(0xfb);
        verify(payload).writeStringLenenc("value");
        verify(payload).writeStringLenenc("1");
        verify(payload).writeStringLenenc(timestamp.toString().split("\\.")[0]);
    }
}
