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

package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConnectionIdGeneratorTest {
    
    private final ConnectionIdGenerator generator = ConnectionIdGenerator.getInstance();
    
    @Test
    public void assertNextId() {
        assertEquals(generator.nextId(), 1);
    }
    
    @Test
    public void assertMaxNextId() throws NoSuchFieldException, IllegalAccessException {
        Field currentId = generator.getClass().getDeclaredField("currentId");
        currentId.setAccessible(true);
        currentId.setInt(generator, 2147483647);
        assertThat(generator.nextId(), is(1));
    }
    
    @Test
    public void assertGetInstance() {
        assertTrue(null != ConnectionIdGenerator.getInstance());
    }
}
