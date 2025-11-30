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

package org.apache.shardingsphere.proxy.backend.state;

import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProxySQLSupportedJudgeEngineTest {
    
    @Test
    void assertIsSupportedWithInSupportedList() {
        assertTrue(new ProxySQLSupportedJudgeEngine(Collections.singleton(SelectStatement.class), Collections.emptyList(), Collections.emptyList(), Collections.emptyList())
                .isSupported(mock(SelectStatement.class)));
    }
    
    @Test
    void assertIsNotSupportedWithInUnsupportedList() {
        assertFalse(new ProxySQLSupportedJudgeEngine(Collections.emptyList(), Collections.emptyList(), Collections.singleton(SelectStatement.class), Collections.emptyList())
                .isSupported(mock(SelectStatement.class)));
    }
    
    @Test
    void assertIsSupportedWithOverlappedList() {
        assertTrue(new ProxySQLSupportedJudgeEngine(Collections.singleton(SelectStatement.class), Collections.emptyList(), Collections.singleton(SQLStatement.class), Collections.emptyList())
                .isSupported(mock(SelectStatement.class)));
    }
    
    @Test
    void assertIsSupportedWithoutList() {
        assertTrue(new ProxySQLSupportedJudgeEngine(Collections.singleton(SelectStatement.class), Collections.emptyList(), Collections.singleton(UpdateStatement.class), Collections.emptyList())
                .isSupported(mock(DeleteStatement.class)));
    }
}
