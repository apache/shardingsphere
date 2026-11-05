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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdServerPreparedStatementTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementFlags")
    void assertGetStatementFlags(final String name, final SQLStatement statement, final int expected) {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(statement);
        FirebirdServerPreparedStatement preparedStatement = new FirebirdServerPreparedStatement("foo_sql", sqlStatementContext, new HintValueContext());
        assertThat(preparedStatement.getStatementFlags(), is(expected));
    }
    
    private static Stream<Arguments> statementFlags() {
        InsertStatement insertSelect = mock(InsertStatement.class);
        when(insertSelect.getInsertSelect()).thenReturn(Optional.of(mock(SubquerySegment.class)));
        InsertStatement insertSelectReturning = mock(InsertStatement.class);
        when(insertSelectReturning.getInsertSelect()).thenReturn(Optional.of(mock(SubquerySegment.class)));
        when(insertSelectReturning.getReturning()).thenReturn(Optional.of(mock(ReturningSegment.class)));
        UpdateStatement updateReturning = mock(UpdateStatement.class);
        when(updateReturning.getReturning()).thenReturn(Optional.of(mock(ReturningSegment.class)));
        DeleteStatement deleteReturning = mock(DeleteStatement.class);
        when(deleteReturning.getReturning()).thenReturn(Optional.of(mock(ReturningSegment.class)));
        ExecuteStatement executeBlockReturning = mock(ExecuteStatement.class);
        when(executeBlockReturning.isReturnsRows()).thenReturn(true);
        return Stream.of(
                Arguments.of("select", mock(SelectStatement.class), 3),
                Arguments.of("ddl", mock(DDLStatement.class), 0),
                Arguments.of("grant", mock(GrantStatement.class), 0),
                Arguments.of("revoke", mock(RevokeStatement.class), 0),
                Arguments.of("create_role", mock(CreateRoleStatement.class), 0),
                Arguments.of("create_user", mock(CreateUserStatement.class), 0),
                Arguments.of("execute_procedure", mock(ExecuteStatement.class), 2),
                Arguments.of("execute_block_returning", executeBlockReturning, 3),
                Arguments.of("insert", mock(InsertStatement.class), 2),
                Arguments.of("insert_select", insertSelect, 2),
                Arguments.of("insert_select_returning", insertSelectReturning, 3),
                Arguments.of("update", mock(UpdateStatement.class), 2),
                Arguments.of("delete", mock(DeleteStatement.class), 2),
                Arguments.of("update_returning", updateReturning, 3),
                Arguments.of("delete_returning", deleteReturning, 3),
                Arguments.of("commit", mock(CommitStatement.class), 2));
    }
}
