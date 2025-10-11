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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.mysql.exception.IncorrectGlobalLocalVariableException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MySQLSystemVariableQueryExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertTryGetSystemVariableQueryExecutorWithOtherExpressionProjection() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        VariableSegment variable = new VariableSegment(0, 0, "max_connections", "session");
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@session.max_connections", variable));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("some_column"))));
        assertFalse(MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement).isPresent());
    }
    
    @Test
    void assertTryGetSystemVariableQueryExecutorWithUnknownVariable() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@unknown_variable", new VariableSegment(0, 0, "unknown_variable")));
        assertFalse(MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement).isPresent());
    }
    
    @Test
    void assertTryGetSystemVariableQueryExecutorAndExecuteWithCorrectScope() throws SQLException {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        VariableSegment maxConnectionsVariable = new VariableSegment(0, 0, "max_connections", "global");
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@global.max_connections", maxConnectionsVariable));
        VariableSegment warningCountVariable = new VariableSegment(0, 0, "warning_count", "session");
        ExpressionProjectionSegment warningCountProjection = new ExpressionProjectionSegment(0, 0, "@@session.warning_count", warningCountVariable);
        warningCountProjection.setAlias(new AliasSegment(0, 0, new IdentifierValue("session_warning")));
        selectStatement.getProjections().getProjections().add(warningCountProjection);
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@max_allowed_packet", new VariableSegment(0, 0, "max_allowed_packet")));
        Optional<DatabaseAdminExecutor> executor = MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement);
        assertTrue(executor.isPresent());
        MySQLSystemVariableQueryExecutor queryExecutor = (MySQLSystemVariableQueryExecutor) executor.get();
        queryExecutor.execute(null, mock());
        QueryResultMetaData actualMetaData = queryExecutor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(3));
        assertThat(actualMetaData.getColumnLabel(1), is("@@global.max_connections"));
        assertThat(actualMetaData.getColumnLabel(2), is("session_warning"));
        assertThat(actualMetaData.getColumnLabel(3), is("@@max_allowed_packet"));
        MergedResult actualResult = queryExecutor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is(MySQLSystemVariable.MAX_CONNECTIONS.getDefaultValue()));
        assertThat(actualResult.getValue(2, String.class), is(MySQLSystemVariable.WARNING_COUNT.getDefaultValue()));
        assertThat(actualResult.getValue(3, String.class), is(MySQLSystemVariable.MAX_ALLOWED_PACKET.getDefaultValue()));
        assertFalse(actualResult.next());
    }
    
    @Test
    void assertExecuteWithIncorrectScope() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        VariableSegment variable = new VariableSegment(0, 0, "max_connections", "session");
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@session.max_connections", variable));
        Optional<DatabaseAdminExecutor> executor = MySQLSystemVariableQueryExecutor.tryGetSystemVariableQueryExecutor(selectStatement);
        assertTrue(executor.isPresent());
        assertThrows(IncorrectGlobalLocalVariableException.class, () -> executor.get().execute(null, mock()));
    }
}
