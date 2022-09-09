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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class PreparedStatementRegistryTest {
    
    @Test
    public void assertAddAndGetAndClosePreparedStatement() {
        PreparedStatement expected = new DummyPreparedStatement();
        PreparedStatementRegistry registry = new PreparedStatementRegistry();
        registry.addPreparedStatement(1, expected);
        assertThat(registry.getPreparedStatement(1), is(expected));
        registry.removePreparedStatement(1);
        assertNull(registry.getPreparedStatement(1));
    }
    
    private static class DummyPreparedStatement implements PreparedStatement {
        
        @Override
        public String getSql() {
            throw new UnsupportedSQLOperationException("");
        }
        
        @Override
        public SQLStatement getSqlStatement() {
            throw new UnsupportedSQLOperationException("");
        }
        
        @Override
        public Optional<SQLStatementContext<?>> getSqlStatementContext() {
            throw new UnsupportedSQLOperationException("");
        }
    }
}
