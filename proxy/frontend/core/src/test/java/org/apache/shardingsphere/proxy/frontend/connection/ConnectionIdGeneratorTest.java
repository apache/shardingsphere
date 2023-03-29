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

package org.apache.shardingsphere.proxy.frontend.connection;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ConnectionIdGeneratorTest {
    
    @BeforeEach
    @AfterEach
    void resetConnectionId() {
        setCurrentConnectionId(0);
    }
    
    @Test
    void assertNextId() {
        assertThat(ConnectionIdGenerator.getInstance().nextId(), is(1));
    }
    
    @Test
    void assertMaxNextId() {
        setCurrentConnectionId(Integer.MAX_VALUE);
        assertThat(ConnectionIdGenerator.getInstance().nextId(), is(1));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setCurrentConnectionId(final int connectionId) {
        Plugins.getMemberAccessor().set(ConnectionIdGenerator.class.getDeclaredField("currentId"), ConnectionIdGenerator.getInstance(), connectionId);
    }
}
