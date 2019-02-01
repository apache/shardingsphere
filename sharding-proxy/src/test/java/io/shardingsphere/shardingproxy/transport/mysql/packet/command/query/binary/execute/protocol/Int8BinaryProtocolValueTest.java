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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol;

import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class Int8BinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertRead() {
        when(payload.readInt8()).thenReturn(1L);
        assertThat(new Int8BinaryProtocolValue().read(payload), CoreMatchers.<Object>is(1L));
    }
    
    @Test
    public void assertWriteWithLong() {
        new Int8BinaryProtocolValue().write(payload, 1L);
        verify(payload).writeInt8(1L);
    }
    
    @Test
    public void assertWriteWithBigDecimal() {
        new Int8BinaryProtocolValue().write(payload, new BigDecimal(1L));
        verify(payload).writeInt8(1L);
    }
}
