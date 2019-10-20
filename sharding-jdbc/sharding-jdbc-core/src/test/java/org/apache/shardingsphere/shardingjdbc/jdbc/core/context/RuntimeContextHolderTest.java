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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class RuntimeContextHolderTest {

    private ShardingRuntimeContext context = mock(ShardingRuntimeContext.class);

    @Before
    public void init() {
        RuntimeContextHolder.getInstance().addRuntimeContext(mock(ShardingRuntimeContext.class));
        RuntimeContextHolder.getInstance().addRuntimeContext(mock(MasterSlaveRuntimeContext.class));
        RuntimeContextHolder.getInstance().addRuntimeContext(mock(EncryptRuntimeContext.class));
        RuntimeContextHolder.getInstance().addRuntimeContext(context);
    }

    @Test
    public void assertGetShardingRuntimeContext() {
        Collection<ShardingRuntimeContext> shardingContexts = RuntimeContextHolder.getInstance().getShardingRuntimeContexts();
        assertFalse(shardingContexts.isEmpty());
        for (ShardingRuntimeContext context : shardingContexts) {
            assertNotNull(context);
        }
    }

    @Test
    public void assertGetMasterSlaveContext() {
        Collection<MasterSlaveRuntimeContext> runtimeContext = RuntimeContextHolder.getInstance().getMasterSlaveContexts();
        assertFalse(runtimeContext.isEmpty());
        for (MasterSlaveRuntimeContext wrapper : runtimeContext) {
            assertNotNull(wrapper);
        }
    }

    @Test
    public void assertGetEncryptRuntimeContext() {
        Collection<EncryptRuntimeContext> runtimeContext = RuntimeContextHolder.getInstance().getEncryptRuntimeContexts();
        assertFalse(runtimeContext.isEmpty());
        for (EncryptRuntimeContext context : runtimeContext) {
            assertNotNull(context);
        }
    }

    @Test
    public void removeRuntimeContext() {
        RuntimeContext context = RuntimeContextHolder.getInstance().removeRuntimeContext(this.context);
        assertSame(context, this.context);
    }
}
