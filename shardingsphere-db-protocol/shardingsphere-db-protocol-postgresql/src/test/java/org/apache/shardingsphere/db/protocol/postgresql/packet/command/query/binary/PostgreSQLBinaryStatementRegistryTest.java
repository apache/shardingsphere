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

import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLBinaryStatementRegistryTest {
    
    @Test
    public void assertRegister() {
        String statementId = "stat-id";
        String sql = "select * from t_order";
        PostgreSQLBinaryStatementRegistry.getInstance().register(1);
        PostgreSQLBinaryStatementRegistry.getInstance().register(1, statementId, sql, mock(SQLStatement.class), Collections.emptyList());
        PostgreSQLBinaryStatement binaryStatement = PostgreSQLBinaryStatementRegistry.getInstance().get(1, statementId);
        assertThat(binaryStatement.getSql(), is(sql));
        assertTrue(binaryStatement.getColumnTypes().isEmpty());
    }
    
    @Test
    public void assertGetNotExists() {
        PostgreSQLBinaryStatement binaryStatement = PostgreSQLBinaryStatementRegistry.getInstance().get(1, "stat-no-exists");
        assertThat(binaryStatement.getSqlStatement(), instanceOf(EmptyStatement.class));
    
    }
    
    @Test
    public void assertUnregister() {
        String statementId = "stat-id";
        String sql = "select * from t_order";
        PostgreSQLBinaryStatementRegistry.getInstance().register(1, statementId, sql, mock(SQLStatement.class), Collections.emptyList());
        PostgreSQLBinaryStatement binaryStatement = PostgreSQLBinaryStatementRegistry.getInstance().get(1, statementId);
        assertNotNull(binaryStatement);
        PostgreSQLBinaryStatementRegistry.getInstance().unregister(1, statementId);
        binaryStatement = PostgreSQLBinaryStatementRegistry.getInstance().get(1, "stat-no-exists");
        assertThat(binaryStatement.getSqlStatement(), instanceOf(EmptyStatement.class));
    }
}
