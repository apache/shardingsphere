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

package org.apache.shardingsphere.infra.binder.context.extractor;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementBaseContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementBaseContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementBaseContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementBaseContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SQLStatementContextFactory.class)
class SQLStatementContextExtractorTest {
    
    @Test
    void assertCreateDataModificationBaseContexts() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        CommonTableExpressionSegment select = mock(CommonTableExpressionSegment.class);
        CommonTableExpressionSegment first = mock(CommonTableExpressionSegment.class);
        CommonTableExpressionSegment second = mock(CommonTableExpressionSegment.class);
        DMLStatement firstStatement = mock(DMLStatement.class);
        DMLStatement secondStatement = mock(DMLStatement.class);
        when(first.getDataModification()).thenReturn(firstStatement);
        when(second.getDataModification()).thenReturn(secondStatement);
        SQLStatementContext firstContext = mock(SQLStatementContext.class);
        SQLStatementContext secondContext = mock(SQLStatementContext.class);
        when(SQLStatementContextFactory.newInstance(metaData, firstStatement, "foo_db")).thenReturn(firstContext);
        when(SQLStatementContextFactory.newInstance(metaData, secondStatement, "foo_db")).thenReturn(secondContext);
        Collection<SQLStatementContext> actual = SQLStatementContextExtractor.createDataModificationBaseContexts(Arrays.asList(select, first, second), metaData, "foo_db");
        assertThat(actual, is(Arrays.asList(firstContext, secondContext)));
    }
    
    @Test
    void assertCreateDataModificationBaseContextsWithoutStatements() {
        assertTrue(SQLStatementContextExtractor.createDataModificationBaseContexts(Collections.emptyList(), mock(ShardingSphereMetaData.class), "foo_db").isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDataModificationParentArguments")
    void assertGetDataModificationBaseContexts(final String name, final SQLStatementContext parent, final Collection<SQLStatementContext> expected) {
        assertThat(SQLStatementContextExtractor.getDataModificationBaseContexts(parent), sameInstance(expected));
    }
    
    private static Stream<Arguments> getDataModificationParentArguments() {
        Collection<SQLStatementContext> contexts = Collections.singletonList(mock(SQLStatementContext.class));
        SelectStatementBaseContext select = mock(SelectStatementBaseContext.class);
        InsertStatementBaseContext insert = mock(InsertStatementBaseContext.class);
        UpdateStatementBaseContext update = mock(UpdateStatementBaseContext.class);
        DeleteStatementBaseContext delete = mock(DeleteStatementBaseContext.class);
        SelectStatementContext selectParent = mock(SelectStatementContext.class);
        when(selectParent.getBaseContext()).thenReturn(select);
        when(select.getDataModificationBaseContexts()).thenReturn(contexts);
        InsertStatementContext insertParent = mock(InsertStatementContext.class);
        when(insertParent.getBaseContext()).thenReturn(insert);
        when(insert.getDataModificationBaseContexts()).thenReturn(contexts);
        UpdateStatementContext updateParent = mock(UpdateStatementContext.class);
        when(updateParent.getBaseContext()).thenReturn(update);
        when(update.getDataModificationBaseContexts()).thenReturn(contexts);
        DeleteStatementContext deleteParent = mock(DeleteStatementContext.class);
        when(deleteParent.getBaseContext()).thenReturn(delete);
        when(delete.getDataModificationBaseContexts()).thenReturn(contexts);
        return Stream.of(Arguments.of("SELECT", selectParent, contexts), Arguments.of("INSERT", insertParent, contexts), Arguments.of("UPDATE", updateParent, contexts),
                Arguments.of("DELETE", deleteParent, contexts));
    }
    
    @Test
    void assertGetDataModificationBaseContextsForOtherStatement() {
        SQLStatementContext parent = mock(SQLStatementContext.class);
        assertTrue(SQLStatementContextExtractor.getDataModificationBaseContexts(parent).isEmpty());
    }
    
    @Test
    void assertGetTableNamesWithDatabaseDefaultSchema() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_idx")));
        IndexSQLStatementAttribute indexSQLStatementAttribute = mock(IndexSQLStatementAttribute.class);
        when(indexSQLStatementAttribute.getIndexes()).thenReturn(Collections.singleton(indexSegment));
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(indexSQLStatementAttribute));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("foo_idx", Collections.emptyList(), false)), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_default_schema", mock(DatabaseType.class), Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        when(database.getSchema("foo_default_schema")).thenReturn(schema);
        assertThat(SQLStatementContextExtractor.getTableNames(database, sqlStatementContext), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetAllSubqueryContextsForSelectStatement() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(subquerySelectStatementContext.getSubqueryContexts()).thenReturn(Collections.emptyMap());
        when(selectStatementContext.getSubqueryContexts()).thenReturn(Collections.singletonMap(0, subquerySelectStatementContext));
        Collection<SelectStatementContext> actual = SQLStatementContextExtractor.getAllSubqueryContexts(selectStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGetAllSubqueryContextsForInsertStatement() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getBaseContext()).thenReturn(mock(InsertStatementBaseContext.class));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSubqueryContexts()).thenReturn(Collections.singletonMap(0, mock(SelectStatementContext.class)));
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        Collection<SelectStatementContext> actual = SQLStatementContextExtractor.getAllSubqueryContexts(insertStatementContext);
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetAllInsertBaseSubqueryContextsWithInsertValues() {
        InsertStatementContext insert = mock(InsertStatementContext.class);
        InsertStatementBaseContext base = mock(InsertStatementBaseContext.class);
        when(insert.getBaseContext()).thenReturn(base);
        SelectStatementContext common = mock(SelectStatementContext.class);
        SelectStatementContext nested = mock(SelectStatementContext.class);
        when(common.getSubqueryContexts()).thenReturn(Collections.singletonMap(20, nested));
        when(base.getSubqueryContexts()).thenReturn(Collections.singletonMap(10, common));
        Collection<SelectStatementContext> actual = SQLStatementContextExtractor.getAllSubqueryContexts(insert);
        assertThat(actual, is(Arrays.asList(common, nested)));
    }
    
    @Test
    void assertGetAllInsertBaseSubqueryContextsWithInsertSelect() {
        InsertStatementContext insert = mock(InsertStatementContext.class);
        InsertStatementBaseContext base = mock(InsertStatementBaseContext.class);
        when(insert.getBaseContext()).thenReturn(base);
        SelectStatementContext common = mock(SelectStatementContext.class);
        SelectStatementContext nested = mock(SelectStatementContext.class);
        when(common.getSubqueryContexts()).thenReturn(Collections.singletonMap(20, nested));
        when(base.getSubqueryContexts()).thenReturn(Collections.singletonMap(10, common));
        SelectStatementContext source = mock(SelectStatementContext.class);
        InsertSelectContext select = mock(InsertSelectContext.class);
        when(select.getSelectStatementContext()).thenReturn(source);
        when(insert.getInsertSelectContext()).thenReturn(select);
        Collection<SelectStatementContext> actual = SQLStatementContextExtractor.getAllSubqueryContexts(insert);
        assertThat(actual, is(Arrays.asList(source, common, nested)));
    }
    
    @Test
    void assertGetWhereSegments() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        WhereSegment whereSegment1 = mock(WhereSegment.class);
        when(whereSegment1.getStartIndex()).thenReturn(1);
        when(whereSegment1.getStopIndex()).thenReturn(1);
        when(selectStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment1));
        WhereSegment whereSegment2 = mock(WhereSegment.class);
        when(whereSegment2.getStartIndex()).thenReturn(2);
        when(whereSegment2.getStopIndex()).thenReturn(2);
        when(subquerySelectStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment2));
        Collection<WhereSegment> actual = SQLStatementContextExtractor.getWhereSegments(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetColumnSegment() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getColumnSegments()).thenReturn(Collections.singleton(mock(ColumnSegment.class)));
        when(subquerySelectStatementContext.getColumnSegments()).thenReturn(Collections.singleton(mock(ColumnSegment.class)));
        Collection<ColumnSegment> actual = SQLStatementContextExtractor.getColumnSegments(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetJoinConditions() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getJoinConditions()).thenReturn(Collections.singleton(mock(BinaryOperationExpression.class)));
        when(subquerySelectStatementContext.getJoinConditions()).thenReturn(Collections.singleton(mock(BinaryOperationExpression.class)));
        Collection<BinaryOperationExpression> actual = SQLStatementContextExtractor.getJoinConditions(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
}
