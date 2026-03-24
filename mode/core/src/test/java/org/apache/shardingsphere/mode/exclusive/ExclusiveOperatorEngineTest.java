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

package org.apache.shardingsphere.mode.exclusive;

import org.apache.shardingsphere.mode.exclusive.callback.ExclusiveOperationCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExclusiveOperatorEngineTest {
    
    private StubExclusiveOperatorContext context;
    
    private ExclusiveOperatorEngine engine;
    
    @BeforeEach
    void setUp() {
        context = new StubExclusiveOperatorContext();
        engine = new ExclusiveOperatorEngine(context);
    }
    
    @Test
    void assertOperate() throws SQLException {
        long timeoutMillis = 100L;
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        context.startResult = Optional.of(() -> context.closed.set(true));
        engine.operate(() -> "foo_operation", timeoutMillis, () -> callbackExecuted.set(true));
        assertTrue(callbackExecuted.get());
        assertThat(context.actualOperationKey, is("/exclusive_operation/foo_operation"));
        assertThat(context.actualTimeoutMillis, is(timeoutMillis));
        assertTrue(context.closed.get());
    }
    
    @Test
    void assertOperateWithResult() throws SQLException {
        @SuppressWarnings("unchecked")
        ExclusiveOperationCallback<String> callback = () -> "ignored";
        long timeoutMillis = 100L;
        context.startResult = Optional.empty();
        assertNull(engine.operateWithResult(() -> "foo_operation", timeoutMillis, callback));
        assertThat(context.actualOperationKey, is("/exclusive_operation/foo_operation"));
        assertThat(context.actualTimeoutMillis, is(timeoutMillis));
        assertFalse(context.closed.get());
    }
    
    private static final class StubExclusiveOperatorContext implements ExclusiveOperatorContext {
        
        private String actualOperationKey;
        
        private long actualTimeoutMillis;
        
        private Optional<ExclusiveLockHandle> startResult = Optional.empty();
        
        private final AtomicBoolean closed = new AtomicBoolean(false);
        
        @Override
        public Optional<ExclusiveLockHandle> start(final String operationKey, final long timeoutMillis) {
            actualOperationKey = operationKey;
            actualTimeoutMillis = timeoutMillis;
            return startResult;
        }
    }
}
