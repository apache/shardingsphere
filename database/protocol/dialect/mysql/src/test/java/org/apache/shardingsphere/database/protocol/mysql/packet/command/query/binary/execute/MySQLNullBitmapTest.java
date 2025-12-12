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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLNullBitmapTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewMySQLNullBitmapFromPayload() {
        when(payload.readInt1()).thenReturn(0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80);
        MySQLNullBitmap actual = new MySQLNullBitmap(64, payload);
        assertTrue(actual.isNullParameter(0));
        assertTrue(actual.isNullParameter(9));
        assertTrue(actual.isNullParameter(63));
        assertFalse(actual.isNullParameter(1));
        assertFalse(actual.isNullParameter(62));
    }
    
    @Test
    void assertGetNullBitmap() {
        MySQLNullBitmap actual = new MySQLNullBitmap(8, 0);
        assertThat(actual.getNullBitmap().length, is(1));
        actual = new MySQLNullBitmap(9, 0);
        assertThat(actual.getNullBitmap().length, is(2));
    }
    
    @Test
    void assertIsNotNullParameter() {
        MySQLNullBitmap actual = new MySQLNullBitmap(8, 0);
        assertFalse(actual.isNullParameter(0));
    }
    
    @Test
    void assertIsNullParameter() {
        MySQLNullBitmap actual = new MySQLNullBitmap(8, 0);
        actual.setNullBit(0);
        assertTrue(actual.isNullParameter(0));
    }
}
