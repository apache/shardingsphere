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

package org.apache.shardingsphere.infra.session.connection.cursor;

import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CursorConnectionContextTest {
    
    @SuppressWarnings("resource")
    @Test
    void assertRemoveCursor() {
        CursorConnectionContext cursorConnectionContext = createCursorConnectionContext();
        cursorConnectionContext.removeCursor("foo");
        assertFalse(cursorConnectionContext.getOrderByValueGroups().containsKey("foo"));
        assertFalse(cursorConnectionContext.getMinGroupRowCounts().containsKey("foo"));
        assertFalse(cursorConnectionContext.getCursorStatementContexts().containsKey("foo"));
        assertFalse(cursorConnectionContext.getExecutedAllDirections().containsKey("foo"));
    }
    
    @Test
    void assertClose() {
        CursorConnectionContext cursorConnectionContext = createCursorConnectionContext();
        cursorConnectionContext.close();
        assertTrue(cursorConnectionContext.getOrderByValueGroups().isEmpty());
        assertTrue(cursorConnectionContext.getMinGroupRowCounts().isEmpty());
        assertTrue(cursorConnectionContext.getCursorStatementContexts().isEmpty());
        assertTrue(cursorConnectionContext.getExecutedAllDirections().isEmpty());
    }
    
    private CursorConnectionContext createCursorConnectionContext() {
        CursorConnectionContext result = new CursorConnectionContext();
        result.getOrderByValueGroups().put("foo", Collections.emptyList());
        result.getMinGroupRowCounts().put("foo", 1L);
        result.getCursorStatementContexts().put("foo", mock(CursorStatementContext.class));
        result.getExecutedAllDirections().put("foo", false);
        return result;
    }
}
