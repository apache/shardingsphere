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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExclusiveOperatorEngineTest {
    
    @Mock
    private ExclusiveOperatorContext context;
    
    private ExclusiveOperatorEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new ExclusiveOperatorEngine(context);
    }
    
    @Test
    void assertOperate() throws SQLException {
        long timeoutMillis = 100L;
        when(context.start("/exclusive_operation/foo_operation", timeoutMillis)).thenReturn(true);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        engine.operate(() -> "foo_operation", timeoutMillis, () -> callbackExecuted.set(true));
        assertTrue(callbackExecuted.get());
        verify(context).start("/exclusive_operation/foo_operation", timeoutMillis);
        verify(context).stop("/exclusive_operation/foo_operation");
    }
    
    @Test
    void assertOperateWithResult() throws SQLException {
        @SuppressWarnings("unchecked")
        ExclusiveOperationCallback<String> callback = mock(ExclusiveOperationCallback.class);
        long timeoutMillis = 100L;
        assertNull(engine.operateWithResult(() -> "foo_operation", timeoutMillis, callback));
        verify(context).start("/exclusive_operation/foo_operation", timeoutMillis);
        verify(context, never()).stop(anyString());
        verify(callback, never()).execute();
    }
}
