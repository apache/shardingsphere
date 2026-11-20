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

package org.apache.shardingsphere.infra.rewrite.context;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SQLRewriteContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private SQLToken sqlToken;
    
    @SuppressWarnings("rawtypes")
    @Mock
    private OptionalSQLTokenGenerator optionalSQLTokenGenerator;
    
    @SuppressWarnings("rawtypes")
    @Mock
    private CollectionSQLTokenGenerator collectionSQLTokenGenerator;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Collections.singleton(sqlToken));
        when(database.getName()).thenReturn("foo_db");
        when(database.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("test", mock(DatabaseType.class))));
    }
    
    @Test
    void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.emptyList());
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), isA(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertInsertWithPrefixParameters() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList("foo_value")));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        when(statementContext.getInsertValueContexts().get(0).getParametersOffset()).thenReturn(2);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getParameters()).thenReturn(Arrays.asList("foo_cte", "bar_cte", "foo_value"));
        SQLRewriteContext actual = new SQLRewriteContext(database, queryContext);
        GroupedParameterBuilder actualBuilder = (GroupedParameterBuilder) actual.getParameterBuilder();
        assertThat(actualBuilder.getBeforeGenericParameterBuilder().getParameters(), is(Arrays.asList("foo_cte", "bar_cte")));
        assertThat(actualBuilder.getAfterGenericParameterOffset(), is(3));
        assertThat(actualBuilder.getParameters(), is(Arrays.asList("foo_cte", "bar_cte", "foo_value")));
    }
    
    @Test
    void assertDefaultValuesWithPrefixParameters() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.emptyList());
        when(statementContext.getInsertValueContexts()).thenReturn(Collections.emptyList());
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        when(statementContext.getSqlStatement().getTable().isPresent()).thenReturn(true);
        when(statementContext.getSqlStatement().getTable().get().getStartIndex()).thenReturn(40);
        when(statementContext.getSqlStatement().getReturning()).thenReturn(Optional.of(new ReturningSegment(50, 60, new ProjectionsSegment(60, 60))));
        when(statementContext.getSqlStatement().getParameterMarkers()).thenReturn(Arrays.asList(new ParameterMarkerExpressionSegment(5, 5, 0), new ParameterMarkerExpressionSegment(60, 60, 1)));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getParameters()).thenReturn(Arrays.asList("foo_cte", "bar_returning"));
        SQLRewriteContext actual = new SQLRewriteContext(database, queryContext);
        assertThat(actual.getParameterBuilder().getParameters(), is(Arrays.asList("foo_cte", "bar_returning")));
    }
    
    @Test
    void assertInsertReturningWithoutBoundParameters() {
        InsertStatement statement = InsertStatement.builder().databaseType(databaseType).returning(new ReturningSegment(30, 45, new ProjectionsSegment(40, 45))).build();
        statement.addParameterMarkers(Collections.singletonList(new ParameterMarkerExpressionSegment(45, 45, 0)));
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        when(statementContext.getSqlStatement()).thenReturn(statement);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        SQLRewriteContext actual = new SQLRewriteContext(database, queryContext);
        assertTrue(actual.getParameterBuilder().getParameters().isEmpty());
    }
    
    @Test
    void assertInsertOnConflictTargetPredicateParameters() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.singletonList(3));
        InsertStatement insertStatement = mock(InsertStatement.class);
        ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(50, 55, Collections.emptyList(), new ParameterMarkerExpressionSegment(54, 54, 2));
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(25, 60, Collections.singletonList(assignment))));
        when(insertStatement.getReturning()).thenReturn(Optional.of(new ReturningSegment(65, 75, new ProjectionsSegment(75, 75))));
        when(insertStatement.getParameterMarkers()).thenReturn(Arrays.asList(new ParameterMarkerExpressionSegment(12, 12, 0), new ParameterMarkerExpressionSegment(35, 35, 1),
                new ParameterMarkerExpressionSegment(54, 54, 2), new ParameterMarkerExpressionSegment(75, 75, 3)));
        when(statementContext.getSqlStatement()).thenReturn(insertStatement);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getParameters()).thenReturn(Arrays.asList(1, 2, 3, 4));
        SQLRewriteContext actual = new SQLRewriteContext(database, queryContext);
        assertThat(actual.getParameterBuilder().getParameters(), is(Arrays.asList(1, 2, 3, 4)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getConflictParameterCases")
    void assertConflictParameterOrder(final String name, final boolean targetPredicate, final String action, final List<Object> expectedParams) {
        String sql = "INSERT INTO foo_table (foo) VALUES (?) ON CONFLICT (foo)" + (targetPredicate ? " WHERE foo > ?" : "") + action;
        boolean update = action.startsWith(" DO UPDATE");
        boolean actionPredicate = action.contains(" WHERE ");
        List<ParameterMarkerSegment> markers = new ArrayList<>();
        for (int position = sql.indexOf('?'); position >= 0; position = sql.indexOf('?', position + 1)) {
            markers.add(new ParameterMarkerExpressionSegment(position, position, markers.size()));
        }
        int assignmentIndex = targetPredicate ? 2 : 1;
        OnDuplicateKeyColumnsSegment conflict = new OnDuplicateKeyColumnsSegment(sql.indexOf("ON CONFLICT"), sql.length() - 1,
                update ? Collections.singletonList(new ColumnAssignmentSegment(sql.indexOf("foo ="), markers.get(assignmentIndex).getStopIndex(), Collections.emptyList(),
                        (ParameterMarkerExpressionSegment) markers.get(assignmentIndex))) : Collections.emptyList());
        if (targetPredicate) {
            conflict.setConflictTargetWhere(createConflictWhere(sql.indexOf("WHERE"), (ParameterMarkerExpressionSegment) markers.get(1)));
        }
        InsertStatement statement = InsertStatement.builder().databaseType(databaseType).onDuplicateKeyColumns(conflict)
                .where(actionPredicate ? createConflictWhere(sql.lastIndexOf("WHERE"), (ParameterMarkerExpressionSegment) markers.get(markers.size() - 1)) : null).build();
        statement.addParameterMarkers(markers);
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getSqlStatement()).thenReturn(statement);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(expectedParams.get(0))));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(update ? Collections.singletonList(expectedParams.get(assignmentIndex)) : Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getParameters()).thenReturn(expectedParams);
        SQLRewriteContext actual = new SQLRewriteContext(database, queryContext);
        assertThat(actual.getParameterBuilder().getParameters(), is(expectedParams));
    }
    
    private static Stream<Arguments> getConflictParameterCases() {
        return Stream.of(Arguments.of("target predicate and do nothing", true, " DO NOTHING", Arrays.<Object>asList(1, 2)),
                Arguments.of("target predicate and conditional update", true, " DO UPDATE SET foo = ? WHERE foo > ?", Arrays.<Object>asList(1, 2, 3, 4)),
                Arguments.of("conditional update", false, " DO UPDATE SET foo = ? WHERE foo > ?", Arrays.<Object>asList(1, 3, 4)),
                Arguments.of("target predicate and conditional select", true, " DO SELECT WHERE foo > ? RETURNING foo", Arrays.<Object>asList(1, 2, 3)));
    }
    
    private WhereSegment createConflictWhere(final int startIndex, final ParameterMarkerExpressionSegment marker) {
        return new WhereSegment(startIndex, marker.getStopIndex(), new BinaryOperationExpression(startIndex + 6, marker.getStopIndex(),
                new ColumnSegment(startIndex + 6, startIndex + 8, new IdentifierValue("foo")), marker, ">", "foo > ?"));
    }
    
    @Test
    void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM tbl WHERE id = ?");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), isA(StandardParameterBuilder.class));
    }
    
    @Test
    void assertGenerateOptionalSQLToken() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), isA(SQLToken.class));
    }
    
    @Test
    void assertGenerateCollectionSQLToken() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), isA(SQLToken.class));
    }
}
