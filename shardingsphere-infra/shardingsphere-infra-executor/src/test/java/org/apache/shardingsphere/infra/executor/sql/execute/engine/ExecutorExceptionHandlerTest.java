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

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ExecutorExceptionHandlerTest {
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field field = ExecutorExceptionHandler.class.getDeclaredField("IS_EXCEPTION_THROWN");
        field.setAccessible(true);
        ((ThreadLocal) field.get(ExecutorExceptionHandler.class)).remove();
    }
    
    @Test(expected = SQLException.class)
    public void assertHandleExceptionWithoutSet() throws SQLException {
        assertTrue(ExecutorExceptionHandler.isExceptionThrown());
        ExecutorExceptionHandler.handleException(new SQLException(""));
    }
    
    @Test(expected = SQLException.class)
    public void assertHandleExceptionWhenExceptionThrownIsTrue() throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(true);
        assertTrue(ExecutorExceptionHandler.isExceptionThrown());
        ExecutorExceptionHandler.handleException(new SQLException(""));
    }
    
    @Test
    public void assertHandleExceptionWhenExceptionThrownIsFalse() throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(false);
        assertFalse(ExecutorExceptionHandler.isExceptionThrown());
        ExecutorExceptionHandler.handleException(new SQLException(""));
    }
}
