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

package org.apache.shardingsphere.proxy.backend.state.impl;

import org.apache.shardingsphere.mode.exception.ShardingSphereStateException;
import org.apache.shardingsphere.proxy.backend.state.type.ReadOnlyProxyStateChecker;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ReadOnlyProxyStateTest {
    
    @Test
    void assertExecuteWithUnsupportedSQL() {
        assertThrows(ShardingSphereStateException.class, () -> new ReadOnlyProxyStateChecker().check(mock(InsertStatement.class), mock()));
    }
    
    @Test
    void assertExecuteWithSupportedSQL() {
        new ReadOnlyProxyStateChecker().check(mock(SelectStatement.class), mock());
    }
}
