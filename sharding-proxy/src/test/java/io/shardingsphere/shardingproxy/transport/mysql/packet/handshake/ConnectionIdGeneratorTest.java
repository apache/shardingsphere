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

package io.shardingsphere.shardingproxy.transport.mysql.packet.handshake;

import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class ConnectionIdGeneratorTest {
    
    @Before
    @After
    public void resetConnectionId() {
        setCurrentConnectionId(0);
    }
    
    @Test
    public void assertNextId() {
        assertEquals(ConnectionIdGenerator.getInstance().nextId(), 1);
    }
    
    @Test
    public void assertMaxNextId() {
        setCurrentConnectionId(Integer.MAX_VALUE);
        assertThat(ConnectionIdGenerator.getInstance().nextId(), is(1));
    }
    
    @SneakyThrows
    private void setCurrentConnectionId(final int connectionId) {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), connectionId);
    }
}
