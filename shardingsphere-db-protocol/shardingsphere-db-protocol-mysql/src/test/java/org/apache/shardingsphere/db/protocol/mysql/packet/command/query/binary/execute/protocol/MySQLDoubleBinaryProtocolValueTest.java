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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLDoubleBinaryProtocolValueTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertRead() {
        when(byteBuf.readDoubleLE()).thenReturn(1.0d);
        assertThat(new MySQLDoubleBinaryProtocolValue().read(new MySQLPacketPayload(byteBuf)), is(1.0d));
    }
    
    @Test
    public void assertWrite() {
        new MySQLDoubleBinaryProtocolValue().write(new MySQLPacketPayload(byteBuf), 1.0d);
        verify(byteBuf).writeDoubleLE(1.0d);
    }
}
