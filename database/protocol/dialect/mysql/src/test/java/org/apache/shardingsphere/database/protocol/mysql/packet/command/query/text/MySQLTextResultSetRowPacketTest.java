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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTextResultSetRowPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNew() {
        when(payload.readStringLenenc()).thenReturn("value_a", null, "value_c");
        new MySQLTextResultSetRowPacket(payload, 3);
        verify(payload, times(3)).readStringLenenc();
    }
    
    @Test
    void assertWrite() {
        long now = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(now);
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(Arrays.asList(null, "value", BigDecimal.ONE, new byte[]{}, timestamp, Boolean.TRUE));
        actual.write(payload);
        verify(payload).writeInt1(0xfb);
        verify(payload).writeStringLenenc("value");
        verify(payload).writeStringLenenc("1");
        if (0 == timestamp.getNanos()) {
            verify(payload).writeStringLenenc(timestamp.toString().split("\\.")[0]);
        } else {
            verify(payload).writeStringLenenc(timestamp.toString());
        }
        verify(payload).writeBytesLenenc(new byte[]{1});
    }
    
    @Test
    void assertTimestampWithoutNanos() {
        long now = System.currentTimeMillis() / 1000L * 1000L;
        Timestamp timestamp = new Timestamp(now);
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(Arrays.asList(null, "value", BigDecimal.ONE, new byte[]{}, timestamp));
        actual.write(payload);
        verify(payload).writeInt1(0xfb);
        verify(payload).writeStringLenenc("value");
        verify(payload).writeStringLenenc("1");
        verify(payload).writeStringLenenc(timestamp.toString().split("\\.")[0]);
    }
    
    @Test
    void assertLocalDateTime() {
        String localDateTimeStr = "2021-08-23T17:30:30";
        LocalDateTime dateTime = LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        MySQLTextResultSetRowPacket actual = new MySQLTextResultSetRowPacket(Collections.singletonList(dateTime));
        actual.write(payload);
        verify(payload).writeStringLenenc(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
    }
}
