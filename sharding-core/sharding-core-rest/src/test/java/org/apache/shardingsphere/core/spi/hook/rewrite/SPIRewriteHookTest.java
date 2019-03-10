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

package org.apache.shardingsphere.core.spi.hook.rewrite;

import org.apache.shardingsphere.core.spi.fixture.RewriteHookFixture;
import org.apache.shardingsphere.core.spi.hook.SPIRewriteHook;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class SPIRewriteHookTest {
    
    private SPIRewriteHook spiRewriteHook;
    
    @Before
    public void setUp() {
        RewriteHookFixture.clearActions();
        spiRewriteHook = new SPIRewriteHook();
    }
    
    @Test
    public void assertStart() {
        spiRewriteHook.start(null);
        assertTrue(RewriteHookFixture.containsAction("start"));
    }
    
    @Test
    public void assertFinishSuccess() {
        spiRewriteHook.finishSuccess(null);
        assertTrue(RewriteHookFixture.containsAction("finishSuccess"));
    }
    
    @Test
    public void assertFinishFailure() {
        spiRewriteHook.finishFailure(null);
        assertTrue(RewriteHookFixture.containsAction("finishFailure"));
    }
}
