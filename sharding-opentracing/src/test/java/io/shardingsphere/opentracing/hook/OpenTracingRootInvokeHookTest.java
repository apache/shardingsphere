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

package io.shardingsphere.opentracing.hook;

import io.shardingsphere.core.executor.ShardingExecuteDataMap;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import io.shardingsphere.spi.root.RootInvokeHook;
import io.shardingsphere.spi.root.SPIRootInvokeHook;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class OpenTracingRootInvokeHookTest extends BaseOpenTracingHookTest {
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @BeforeClass
    public static void registerSPI() {
        NewInstanceServiceLoader.register(RootInvokeHook.class);
    }
    
    @Test
    public void assertRootInvoke() {
        rootInvokeHook.start();
        assertTrue(ShardingExecuteDataMap.getDataMap().containsKey(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION));
        rootInvokeHook.finish(1);
    }
}
