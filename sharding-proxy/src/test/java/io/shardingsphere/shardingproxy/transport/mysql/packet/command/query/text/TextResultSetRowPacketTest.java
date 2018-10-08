/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text;

import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TextResultSetRowPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertWrite() {
        when(payload.readInt1()).thenReturn(1);
        when(payload.readStringLenenc()).thenReturn("value_a", null, "value_c");
        TextResultSetRowPacket actual = new TextResultSetRowPacket(payload, 3);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeStringLenenc("value_a");
        verify(payload).writeStringLenenc("value_c");
        verify(payload).writeInt1(0xfb);
    }
}
