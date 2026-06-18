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

package org.apache.shardingsphere.shadow.route.retriever.dml;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowDeleteStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowInsertStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowSelectStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowUpdateStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.hint.ShadowTableHintDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowDMLStatementDataSourceMappingsRetrieverTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("shadowColumnRetrieverTypeArguments")
    void assertShadowColumnRetrieverType(final String name, final SQLStatementContext sqlStatementContext, final Class<?> expectedRetrieverType) throws ReflectiveOperationException {
        Object actualShadowColumnRetriever = Plugins.getMemberAccessor().get(
                ShadowDMLStatementDataSourceMappingsRetriever.class.getDeclaredField("shadowColumnDataSourceMappingsRetriever"),
                new ShadowDMLStatementDataSourceMappingsRetriever(createQueryContext(sqlStatementContext), ShadowOperationType.INSERT));
        if (null == expectedRetrieverType) {
            assertNull(actualShadowColumnRetriever);
        } else {
            assertThat(actualShadowColumnRetriever, isA(expectedRetrieverType));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("retrieveArguments")
    void assertRetrieve(final String name,
                        final Map<String, String> tableHintResult, final Map<String, String> columnRetrieverResult, final boolean hasShadowColumnRetriever, final Map<String, String> expected) {
        ShadowDMLStatementDataSourceMappingsRetriever retriever = new ShadowDMLStatementDataSourceMappingsRetriever(createQueryContext(createOtherSqlStatementContext()), ShadowOperationType.INSERT);
        ShadowRule rule = mock(ShadowRule.class);
        Collection<String> shadowTables = Collections.singleton("t_order");
        ShadowTableHintDataSourceMappingsRetriever tableHintRetriever = mock(ShadowTableHintDataSourceMappingsRetriever.class);
        setField(retriever, "tableHintDataSourceMappingsRetriever", tableHintRetriever);
        when(rule.filterShadowTables(Collections.singleton("t_order"))).thenReturn(shadowTables);
        when(tableHintRetriever.retrieve(rule, shadowTables)).thenReturn(tableHintResult);
        if (hasShadowColumnRetriever) {
            ShadowColumnDataSourceMappingsRetriever shadowColumnRetriever = mock(ShadowColumnDataSourceMappingsRetriever.class);
            when(shadowColumnRetriever.retrieve(rule, shadowTables)).thenReturn(columnRetrieverResult);
            setField(retriever, "shadowColumnDataSourceMappingsRetriever", shadowColumnRetriever);
        } else {
            setField(retriever, "shadowColumnDataSourceMappingsRetriever", null);
        }
        Map<String, String> actual = retriever.retrieve(rule);
        assertThat(name, actual, is(expected));
    }
    
    private QueryContext createQueryContext(final SQLStatementContext sqlStatementContext) {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, mock());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void setField(final ShadowDMLStatementDataSourceMappingsRetriever target, final String fieldName, final Object fieldValue) {
        Plugins.getMemberAccessor().set(ShadowDMLStatementDataSourceMappingsRetriever.class.getDeclaredField(fieldName), target, fieldValue);
    }
    
    private static Stream<Arguments> shadowColumnRetrieverTypeArguments() {
        return Stream.of(
                Arguments.of("insert statement context", createInsertSqlStatementContext(), ShadowInsertStatementDataSourceMappingsRetriever.class),
                Arguments.of("delete statement context", createDeleteSqlStatementContext(), ShadowDeleteStatementDataSourceMappingsRetriever.class),
                Arguments.of("update statement context", createUpdateSqlStatementContext(), ShadowUpdateStatementDataSourceMappingsRetriever.class),
                Arguments.of("select statement context", createSelectSqlStatementContext(), ShadowSelectStatementDataSourceMappingsRetriever.class),
                Arguments.of("other statement context", createOtherSqlStatementContext(), null));
    }
    
    private static SQLStatementContext createInsertSqlStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        return result;
    }
    
    private static SQLStatementContext createDeleteSqlStatementContext() {
        DeleteStatementContext result = mock(DeleteStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(DeleteStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        return result;
    }
    
    private static SQLStatementContext createUpdateSqlStatementContext() {
        UpdateStatementContext result = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        return result;
    }
    
    private static SQLStatementContext createSelectSqlStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        return result;
    }
    
    private static SQLStatementContext createOtherSqlStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton("t_order"));
        return result;
    }
    
    private static Stream<Arguments> retrieveArguments() {
        return Stream.of(
                Arguments.of("return table hint result when it is not empty", Collections.singletonMap("foo_prod_ds", "foo_shadow_ds"),
                        Collections.singletonMap("foo_prod_ds", "bar_shadow_ds"), true, Collections.singletonMap("foo_prod_ds", "foo_shadow_ds")),
                Arguments.of("return column retriever result when table hint result is empty",
                        Collections.emptyMap(), Collections.singletonMap("foo_prod_ds", "foo_shadow_ds"), true, Collections.singletonMap("foo_prod_ds", "foo_shadow_ds")),
                Arguments.of("return table hint result when table hint result is empty and shadow column retriever is null",
                        Collections.emptyMap(), Collections.emptyMap(), false, Collections.emptyMap()));
    }
}
