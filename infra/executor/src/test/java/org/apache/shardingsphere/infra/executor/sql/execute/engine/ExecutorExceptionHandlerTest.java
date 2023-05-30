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

package org.apache.shardingsphere.infra.executor.sql.execute.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorExceptionHandlerTest {
    
    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        ((ThreadLocal<?>) Plugins.getMemberAccessor().get(SQLExecutorExceptionHandler.class.getDeclaredField("IS_EXCEPTION_THROWN"), SQLExecutorExceptionHandler.class)).remove();
    }
    
    @Test
    void assertHandleExceptionWithoutSet() {
        assertTrue(SQLExecutorExceptionHandler.isExceptionThrown());
        assertThrows(SQLException.class, () -> SQLExecutorExceptionHandler.handleException(new SQLException("")));
    }
    
    @Test
    void assertHandleExceptionWhenExceptionThrownIsTrue() {
        SQLExecutorExceptionHandler.setExceptionThrown(true);
        assertTrue(SQLExecutorExceptionHandler.isExceptionThrown());
        assertThrows(SQLException.class, () -> SQLExecutorExceptionHandler.handleException(new SQLException("")));
    }
    
    @Test
    void assertHandleExceptionWhenExceptionThrownIsFalse() throws SQLException {
        SQLExecutorExceptionHandler.setExceptionThrown(false);
        assertFalse(SQLExecutorExceptionHandler.isExceptionThrown());
        SQLExecutorExceptionHandler.handleException(new SQLException(""));
    }
}
