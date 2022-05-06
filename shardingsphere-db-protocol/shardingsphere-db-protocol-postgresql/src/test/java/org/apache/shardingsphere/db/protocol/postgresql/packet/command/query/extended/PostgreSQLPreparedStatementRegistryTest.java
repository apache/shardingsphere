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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended;

import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLPreparedStatementRegistryTest {
    
    @Test
    public void assertRegister() {
        String statementId = "stat-id";
        String sql = "select * from t_order";
        PostgreSQLPreparedStatementRegistry.getInstance().register(1);
        PostgreSQLPreparedStatementRegistry.getInstance().register(1, statementId, sql, mock(SQLStatement.class), Collections.emptyList());
        PostgreSQLPreparedStatement preparedStatement = PostgreSQLPreparedStatementRegistry.getInstance().get(1, statementId);
        assertThat(preparedStatement.getSql(), is(sql));
        assertTrue(preparedStatement.getParameterTypes().isEmpty());
    }
    
    @Test
    public void assertCloseStatement() {
        final int connectionId = 2;
        final String statementId = "S_2";
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId);
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId, statementId, "", mock(SQLStatement.class), Collections.emptyList());
        assertNotNull(PostgreSQLPreparedStatementRegistry.getInstance().get(connectionId, statementId));
        PostgreSQLPreparedStatementRegistry.getInstance().closeStatement(connectionId, statementId);
        assertNull(PostgreSQLPreparedStatementRegistry.getInstance().get(connectionId, statementId));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertUnregister() {
        final int connectionId = 3;
        final String statementId = "";
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId);
        PostgreSQLPreparedStatementRegistry.getInstance().get(connectionId, statementId);
        PostgreSQLPreparedStatementRegistry.getInstance().unregister(connectionId);
        PostgreSQLPreparedStatementRegistry.getInstance().get(connectionId, statementId);
    }
}
