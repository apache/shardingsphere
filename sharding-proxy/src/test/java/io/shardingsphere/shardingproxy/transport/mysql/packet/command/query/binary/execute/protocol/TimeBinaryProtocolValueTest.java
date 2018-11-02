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

import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TimeBinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertReadWithZeroByte() {
        assertThat(new TimeBinaryProtocolValue().read(payload), CoreMatchers.<Object>is(new Timestamp(0)));
    }

    @Test
    public void assertReadWithEightBytes() {
        when(payload.readInt1()).thenReturn(8, 0, 10, 59, 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new TimeBinaryProtocolValue().read(payload)).getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }

    @Test
    public void assertReadWithTwelveBytes() {
        when(payload.readInt1()).thenReturn(12, 0, 10, 59, 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new TimeBinaryProtocolValue().read(payload)).getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertReadWithIllegalArgument() {
        when(payload.readInt1()).thenReturn(100);
        new TimeBinaryProtocolValue().read(payload);
    }
}
