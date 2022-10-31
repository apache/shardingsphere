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

package org.apache.shardingsphere.infra.binder.statement.dal;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLExplainStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExplainStatementContextTest {
    
    @Test
    public void assertMySQLNewInstance() {
        assertNewInstance(mock(MySQLExplainStatement.class));
    }
    
    @Test
    public void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLExplainStatement.class));
    }
    
    private void assertNewInstance(final ExplainStatement explainStatement) {
        SQLStatement statement = () -> 0;
        when(explainStatement.getStatement()).thenReturn(Optional.of(statement));
        ExplainStatementContext actual = new ExplainStatementContext(explainStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(explainStatement));
        assertThat(actual.getSqlStatement().getStatement().orElse(null), is(statement));
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
    }
}
