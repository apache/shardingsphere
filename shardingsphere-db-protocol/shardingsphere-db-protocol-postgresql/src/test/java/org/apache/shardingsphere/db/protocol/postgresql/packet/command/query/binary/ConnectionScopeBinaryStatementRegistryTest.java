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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ConnectionScopeBinaryStatementRegistryTest {
    
    @Test
    public void assertRegister() {
        ConnectionScopeBinaryStatementRegistry statementRegistry = new ConnectionScopeBinaryStatementRegistry();
        String statementId = "stat-id";
        String sql = "select * from t_order";
        statementRegistry.register(statementId, sql, 1, null);
        
        PostgreSQLBinaryStatement binaryStatement = statementRegistry.getBinaryStatement(statementId);
        assertThat(binaryStatement.getSql(), is(sql));
        assertThat(binaryStatement.getParametersCount(), is(1));
    }
    
    @Test
    public void assertGetBinaryStatementNotExists() {
        ConnectionScopeBinaryStatementRegistry statementRegistry = new ConnectionScopeBinaryStatementRegistry();
        PostgreSQLBinaryStatement binaryStatement = statementRegistry.getBinaryStatement("stat-no-exists");
        assertNull(binaryStatement);
    }
    
    @Test
    public void assertGetBinaryStatement() {
        ConnectionScopeBinaryStatementRegistry statementRegistry = new ConnectionScopeBinaryStatementRegistry();
        statementRegistry.register("stat-id", "", 1, null);
        PostgreSQLBinaryStatement binaryStatement = statementRegistry.getBinaryStatement("stat-id");
        assertNotNull(binaryStatement);
    }
}
