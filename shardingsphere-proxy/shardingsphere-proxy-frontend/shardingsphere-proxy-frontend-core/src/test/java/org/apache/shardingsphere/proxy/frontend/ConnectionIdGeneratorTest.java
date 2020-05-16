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

package org.apache.shardingsphere.proxy.frontend;

import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConnectionIdGeneratorTest {
    
    @Before
    @After
    public void resetConnectionId() {
        setCurrentConnectionId(0);
    }
    
    @Test
    public void assertNextId() {
        assertThat(ConnectionIdGenerator.getInstance().nextId(), is(1));
    }
    
    @Test
    public void assertMaxNextId() {
        setCurrentConnectionId(Integer.MAX_VALUE);
        assertThat(ConnectionIdGenerator.getInstance().nextId(), is(1));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setCurrentConnectionId(final int connectionId) {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), connectionId);
    }
}
