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

package org.apache.shardingsphere.tracing.opentracing.hook;

import org.apache.shardingsphere.infra.executor.kernel.ExecutorDataMap;
import org.apache.shardingsphere.infra.hook.RootInvokeHook;
import org.apache.shardingsphere.infra.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class OpenTracingRootInvokeHookTest extends BaseOpenTracingHookTest {
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @BeforeClass
    public static void registerSPI() {
        ShardingSphereServiceLoader.register(RootInvokeHook.class);
    }
    
    @Test
    public void assertRootInvoke() {
        rootInvokeHook.start();
        //assertTrue(ExecutorDataMap.getValue().containsKey(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION));
        rootInvokeHook.finish(1);
    }
}
