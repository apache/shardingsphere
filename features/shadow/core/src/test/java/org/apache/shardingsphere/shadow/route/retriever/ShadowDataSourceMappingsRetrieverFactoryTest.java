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

package org.apache.shardingsphere.shadow.route.retriever;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.dml.ShadowDMLStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.hint.ShadowHintDataSourceMappingsRetriever;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowDataSourceMappingsRetrieverFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newInstanceArguments")
    void assertNewInstance(final String name, final SQLStatementContext sqlStatementContext, final Class<? extends ShadowDataSourceMappingsRetriever> expectedRetrieverType) {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShadowDataSourceMappingsRetriever actualRetriever = ShadowDataSourceMappingsRetrieverFactory.newInstance(
                new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, mock(ShardingSphereMetaData.class)));
        assertThat(name, actualRetriever, isA(expectedRetrieverType));
    }
    
    private static Stream<Arguments> newInstanceArguments() {
        return Stream.of(
                Arguments.of("insert statement context", createInsertSqlStatementContext(), ShadowDMLStatementDataSourceMappingsRetriever.class),
                Arguments.of("delete statement context", createDeleteSqlStatementContext(), ShadowDMLStatementDataSourceMappingsRetriever.class),
                Arguments.of("update statement context", createUpdateSqlStatementContext(), ShadowDMLStatementDataSourceMappingsRetriever.class),
                Arguments.of("select statement context", createSelectSqlStatementContext(), ShadowDMLStatementDataSourceMappingsRetriever.class),
                Arguments.of("other statement context", createOtherSqlStatementContext(), ShadowHintDataSourceMappingsRetriever.class));
    }
    
    private static SQLStatementContext createInsertSqlStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private static SQLStatementContext createUpdateSqlStatementContext() {
        UpdateStatementContext result = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private static SQLStatementContext createDeleteSqlStatementContext() {
        DeleteStatementContext result = mock(DeleteStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(DeleteStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private static SQLStatementContext createSelectSqlStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private static SQLStatementContext createOtherSqlStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
}
