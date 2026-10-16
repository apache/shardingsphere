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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptAssignmentTokenGeneratorTest {
    
    private static MockedConstruction<DatabaseTypeRegistry> registryConstruction;
    
    private EncryptAssignmentTokenGenerator tokenGenerator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TablesContext tablesContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnAssignmentSegment assignmentSegment;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SetAssignmentSegment setAssignmentSegment;
    
    @BeforeAll
    static void beforeAll() {
        registryConstruction = mockConstruction(DatabaseTypeRegistry.class, (mock, mockContext) -> {
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
            when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(QuoteCharacter.NONE);
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        });
    }
    
    @AfterAll
    static void afterAll() {
        registryConstruction.close();
    }
    
    @BeforeEach
    void setup() {
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singleton(assignmentSegment));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("columns"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("table"),
                new IdentifierValue("columns"), TableSourceType.PHYSICAL_TABLE));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.getTable()).thenReturn("table");
        when(encryptTable.isEncryptColumn("columns")).thenReturn(true);
        when(encryptTable.getEncryptColumn("columns")).thenReturn(mock(EncryptColumn.class, RETURNS_DEEP_STUBS));
        when(result.findEncryptTable("table")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateParameterMarkerExpressionSegment() {
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(mock(ParameterMarkerExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateLiteralExpressionSegment() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        EncryptRule rule = mockEncryptRule();
        EncryptColumn encryptColumn = rule.findEncryptTable("table").get().getEncryptColumn("columns");
        when(encryptColumn.getName()).thenReturn("columns");
        when(encryptColumn.getCipher().getName()).thenReturn("cipher_columns");
        when(encryptColumn.getCipher().encrypt("foo_db", "foo_default_schema", "table", "columns", Collections.singletonList("foo_value")))
                .thenReturn(Collections.singletonList("encrypted_value"));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(tablesContext.getSchemaName()).thenReturn(Optional.empty());
        when(assignmentSegment.getValue()).thenReturn(new LiteralExpressionSegment(0, 0, "foo_value"));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).iterator().next().toString(), is("cipher_columns = 'encrypted_value'"));
    }
    
    @Test
    void assertGenerateSQLTokenWithUpdateEmpty() {
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(null);
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokenWithInsertLiteralExpressionSegment() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        tokenGenerator = new EncryptAssignmentTokenGenerator(mockEncryptRule(), database, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(assignmentSegment.getValue()).thenReturn(mock(LiteralExpressionSegment.class));
        assertThat(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getEncryptedTupleArguments")
    void assertGenerateSQLTokensWithEncryptedTuple(final String name, final Collection<String> columnNames, final String encryptedColumnName) {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn(anyString())).thenAnswer(invocation -> encryptedColumnName.equals(invocation.getArgument(0)));
        when(assignmentSegment.getColumns()).thenReturn(columnNames.stream().map(this::createBoundColumn).collect(Collectors.toList()));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Encrypted assignments with multiple columns, nested fields or unsupported expressions."));
    }
    
    private ColumnSegment createBoundColumn(final String columnName) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        result.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("foo_table"), new IdentifierValue(columnName), TableSourceType.PHYSICAL_TABLE));
        return result;
    }
    
    private static Stream<Arguments> getEncryptedTupleArguments() {
        return Stream.of(
                Arguments.of("encrypted first target", Arrays.asList("foo_cipher", "foo_plain"), "foo_cipher"),
                Arguments.of("encrypted second target", Arrays.asList("foo_plain", "foo_cipher"), "foo_cipher"),
                Arguments.of("encrypted third target", Arrays.asList("foo_plain", "bar_plain", "foo_cipher"), "foo_cipher"));
    }
    
    @Test
    void assertGenerateSQLTokensWithPlainTuple() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(mock(EncryptTable.class)));
        when(assignmentSegment.getColumns()).thenReturn(Arrays.asList(createBoundColumn("foo_plain"), createBoundColumn("bar_plain")));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithTupleWithoutEncryptTable() {
        when(assignmentSegment.getColumns()).thenReturn(Arrays.asList(createBoundColumn("foo_plain"), createBoundColumn("bar_plain")));
        tokenGenerator = new EncryptAssignmentTokenGenerator(mock(EncryptRule.class), mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSupportedFunctionValues")
    void assertGenerateSQLTokensWithEncryptedFunction(final String name, final FunctionSegment value) {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("foo_cipher")).thenReturn(true);
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(createBoundColumn("foo_cipher")));
        when(assignmentSegment.getValue()).thenReturn(value);
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    private static Stream<Arguments> getSupportedFunctionValues() {
        FunctionSegment ifnull = new FunctionSegment(0, 20, "IFNULL", "IFNULL(foo_cipher, ?)");
        ifnull.getParameters().add(new ColumnSegment(7, 16, new IdentifierValue("foo_cipher")));
        ifnull.getParameters().add(new ParameterMarkerExpressionSegment(19, 19, 0));
        FunctionSegment coalesce = new FunctionSegment(0, 33, "COALESCE", "COALESCE(IFNULL(foo_cipher, ?), 0)");
        coalesce.getParameters().add(ifnull);
        coalesce.getParameters().add(new LiteralExpressionSegment(32, 32, 0));
        return Stream.of(
                Arguments.of("legacy function", ifnull),
                Arguments.of("nested legacy function", coalesce),
                Arguments.of("function without parameters", new FunctionSegment(0, 3, "PI", "PI()")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnsupportedAssignmentValues")
    void assertGenerateSQLTokensWithEncryptedUnsupportedValue(final String name, final ExpressionSegment value) {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("foo_cipher")).thenReturn(true);
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(createBoundColumn("foo_cipher")));
        when(assignmentSegment.getValue()).thenReturn(value);
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Encrypted assignments with multiple columns, nested fields or unsupported expressions."));
    }
    
    private static Stream<Arguments> getUnsupportedAssignmentValues() {
        UnaryOperationExpression unary = new UnaryOperationExpression(0, 1, new ParameterMarkerExpressionSegment(1, 1, 0), "~", "~?");
        FunctionSegment function = new FunctionSegment(0, 11, "coalesce", "coalesce(~?)");
        function.getParameters().add(unary);
        FunctionSegment nestedFunction = new FunctionSegment(0, 21, "coalesce", "coalesce(coalesce(~?))");
        nestedFunction.getParameters().add(function);
        FunctionSegment commonFunction = new FunctionSegment(0, 15, "coalesce", "coalesce((?)[1])");
        commonFunction.getParameters().add(new CommonExpressionSegment(9, 14, "(?)[1]", Collections.singletonList(new ParameterMarkerExpressionSegment(10, 10, 0))));
        return Stream.of(
                Arguments.of("common children", new CommonExpressionSegment(0, 5, "(?)[1]", Collections.singletonList(new ParameterMarkerExpressionSegment(1, 1, 0)))),
                Arguments.of("opaque common", new CommonExpressionSegment(0, 6, "DEFAULT", Collections.emptyList())),
                Arguments.of("cast unary value", new TypeCastExpression(0, 14, "CAST(~? AS text)", unary, "text")),
                Arguments.of("unary value", unary),
                Arguments.of("function unary value", function),
                Arguments.of("nested function unary value", nestedFunction),
                Arguments.of("function common value", commonFunction));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnsupportedAssignmentValues")
    void assertGenerateSQLTokensWithPlainUnsupportedValue(final String name, final ExpressionSegment value) {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(mock(EncryptTable.class)));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(createBoundColumn("foo_plain")), value)));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFieldAssignmentArguments")
    void assertGenerateSQLTokensWithEncryptedField(final String name, final Collection<String> fields, final ExpressionSegment value) {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isEncryptColumn("foo_cipher")).thenReturn(true);
        ColumnSegment column = createBoundColumn("foo_cipher");
        column.setNestedObjectAttributes(fields.stream().map(IdentifierValue::new).collect(Collectors.toList()));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(column), value)));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Encrypted assignments with multiple columns, nested fields or unsupported expressions."));
    }
    
    private static Stream<Arguments> getFieldAssignmentArguments() {
        return Stream.of(
                Arguments.of("single field parameter", Collections.singletonList("foo_field"), new ParameterMarkerExpressionSegment(0, 0, 0)),
                Arguments.of("field chain literal", Arrays.asList("foo_field", "bar_field"), new LiteralExpressionSegment(0, 0, "foo_value")),
                Arguments.of("quoted field parameter", Collections.singletonList("\"foo.field\""), new ParameterMarkerExpressionSegment(0, 0, 0)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFieldAssignmentArguments")
    void assertGenerateSQLTokensWithPlainField(final String name, final Collection<String> fields, final ExpressionSegment value) {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.findEncryptTable("foo_table")).thenReturn(Optional.of(mock(EncryptTable.class)));
        ColumnSegment column = createBoundColumn("foo_plain");
        column.setNestedObjectAttributes(fields.stream().map(IdentifierValue::new).collect(Collectors.toList()));
        when(setAssignmentSegment.getAssignments()).thenReturn(Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(column), value)));
        tokenGenerator = new EncryptAssignmentTokenGenerator(rule, mock(ShardingSphereDatabase.class), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        assertTrue(tokenGenerator.generateSQLTokens(tablesContext, setAssignmentSegment).isEmpty());
    }
}
