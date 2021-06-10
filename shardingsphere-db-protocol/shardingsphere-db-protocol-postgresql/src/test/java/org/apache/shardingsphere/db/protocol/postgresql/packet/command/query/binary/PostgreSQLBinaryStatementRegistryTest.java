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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class PostgreSQLBinaryStatementRegistryTest {
    
    private final String sql = "SELECT * FROM tbl WHERE id=?";
    
    private final String statementId = "stat-id";
    
    @Before
    public void init() {
        PostgreSQLBinaryStatementRegistry.getInstance().register(1);
        ConnectionScopeBinaryStatementRegistry statementRegistry = PostgreSQLBinaryStatementRegistry.getInstance().get(1);
        statementRegistry.register(statementId, sql, 1, null);
    }
    
    @Test
    public void assertRegisterIfAbsent() {
        PostgreSQLBinaryStatementRegistry.getInstance().register(2);
        ConnectionScopeBinaryStatementRegistry actual = PostgreSQLBinaryStatementRegistry.getInstance().get(2);
        assertNull(actual.getBinaryStatement("stat-no-exist"));
    }
    
    @Test
    public void assertRegisterIfPresent() {
        ConnectionScopeBinaryStatementRegistry statementRegistry = PostgreSQLBinaryStatementRegistry.getInstance().get(1);
        PostgreSQLBinaryStatement statement = statementRegistry.getBinaryStatement(statementId);
        assertThat(statement.getSql(), is(sql));
        assertThat(statement.getParameterCount(), is(1));
        PostgreSQLBinaryStatementRegistry.getInstance().register(1);
        assertNull(PostgreSQLBinaryStatementRegistry.getInstance().get(1).getBinaryStatement(statementId));
    }
    
    @Test
    public void assertUnregister() {
        PostgreSQLBinaryStatementRegistry.getInstance().unregister(1);
        ConnectionScopeBinaryStatementRegistry actual = PostgreSQLBinaryStatementRegistry.getInstance().get(1);
        assertNull(actual);
    }
}
