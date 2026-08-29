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
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLStatementContextExtractorTest {
    
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
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSubqueryContexts()).thenReturn(Collections.singletonMap(0, mock(SelectStatementContext.class)));
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        Collection<SelectStatementContext> actual = SQLStatementContextExtractor.getAllSubqueryContexts(insertStatementContext);
        assertThat(actual.size(), is(2));
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
