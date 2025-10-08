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

package org.apache.shardingsphere.proxy.backend.session;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServerPreparedStatementRegistryTest {
    
    @Test
    void assertAddAndGetAndClosePreparedStatement() {
        ServerPreparedStatement expected = new DummyServerPreparedStatement();
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, expected);
        assertThat(registry.getPreparedStatement(1), is(expected));
        registry.removePreparedStatement(1);
        assertNull(registry.getPreparedStatement(1));
    }
    
    @Test
    void assertClearPreparedStatements() {
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, new DummyServerPreparedStatement());
        registry.clear();
        assertNull(registry.getPreparedStatement(1));
    }
    
    private static final class DummyServerPreparedStatement implements ServerPreparedStatement {
        
        @Override
        public String getSql() {
            throw new UnsupportedSQLOperationException("");
        }
        
        @Override
        public SQLStatementContext getSqlStatementContext() {
            throw new UnsupportedSQLOperationException("");
        }
    }
}
