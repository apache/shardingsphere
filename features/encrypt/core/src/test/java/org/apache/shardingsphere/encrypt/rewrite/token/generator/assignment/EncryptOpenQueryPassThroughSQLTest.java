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

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptOpenQueryPassThroughSQLTest {
    
    @Test
    void assertParseWithMultipartTableName() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE foo_col = 1");
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getSchemaName(), is(Optional.of("foo_schema")));
        assertThat(actual.getRemainder(), is(" WHERE foo_col = 1"));
    }
    
    @Test
    void assertParseWithDelimitedMultipartTableName() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM [foo_schema].[foo_tbl]");
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getSchemaName(), is(Optional.of("foo_schema")));
    }
    
    @Test
    void assertParseWithDoubleQuotedMultipartTableName() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM \"foo_schema\".\"foo_tbl\"");
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getSchemaName(), is(Optional.of("foo_schema")));
        assertThat(actual.getTableExpression(), is("\"foo_schema\".\"foo_tbl\""));
    }
    
    @Test
    void assertParseKeepsLogicColumnInWhereClause() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE foo_col IS NOT NULL");
        assertThat(actual.getRemainder(), is(" WHERE foo_col IS NOT NULL"));
    }
    
    @Test
    void assertParseKeepsColumnNameInsideStringLiteral() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE note_col = 'foo_col'");
        assertThat(actual.getRemainder(), is(" WHERE note_col = 'foo_col'"));
    }
    
    @Test
    void assertParseDoesNotRejectCommaInsideInList() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE id_col IN (1, 2, 4)");
        assertThat(actual.getRemainder(), is(" WHERE id_col IN (1, 2, 4)"));
    }
    
    @Test
    void assertParseDoesNotRejectSetOperationKeywordInsideStringLiteral() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE note_col = 'UNION ALL'");
        assertThat(actual.getRemainder(), is(" WHERE note_col = 'UNION ALL'"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findTableNameArguments")
    void assertFindTableNameDiscoversTableWithoutShapeValidation(final String scenario, final String passThroughSQL, final String expectedTableName) {
        Optional<String> actual = EncryptOpenQueryPassThroughSQL.findTableName(passThroughSQL);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expectedTableName));
    }
    
    private static Stream<Arguments> findTableNameArguments() {
        return Stream.of(
                Arguments.of("double quoted multipart", "SELECT foo_col FROM \"foo_schema\".\"foo_tbl\" WHERE id_col = 1", "foo_tbl"),
                Arguments.of("escaped bracket", "SELECT foo_col FROM [foo_schema].[foo]]tbl]", "foo]tbl"),
                Arguments.of("three-part table", "SELECT foo_col FROM foo_db.foo_schema.foo_tbl WHERE id_col = 4", "foo_tbl"),
                Arguments.of("join", "SELECT foo_col FROM foo_schema.foo_tbl JOIN foo_schema.bar_tbl ON foo_tbl.id_col = bar_tbl.id_col", "foo_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedShapeArguments")
    void assertParseRejectsUnsupportedShape(final String scenario, final String passThroughSQL) {
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse(passThroughSQL));
    }
    
    private static Stream<Arguments> unsupportedShapeArguments() {
        return Stream.of(
                Arguments.of("select list literal", "SELECT 'foo_col' FROM foo_schema.foo_tbl"),
                Arguments.of("select list expression", "SELECT UPPER(foo_col) FROM foo_schema.foo_tbl"),
                Arguments.of("space-delimited identifier", "SELECT foo_col FROM [Human Resources].[foo_tbl]"),
                Arguments.of("three-part table", "SELECT foo_col FROM foo_db.foo_schema.foo_tbl"),
                Arguments.of("join", "SELECT foo_col FROM foo_schema.foo_tbl JOIN foo_schema.bar_tbl ON foo_tbl.id_col = bar_tbl.id_col"),
                Arguments.of("comma table sources", "SELECT foo_col FROM foo_schema.foo_tbl, foo_schema.bar_tbl"),
                Arguments.of("alias then comma table sources", "SELECT foo_col FROM foo_schema.foo_tbl AS t, foo_schema.bar_tbl"),
                Arguments.of("cross apply", "SELECT foo_col FROM foo_schema.foo_tbl CROSS APPLY foo_schema.fn_bar(foo_tbl.id_col)"),
                Arguments.of("outer apply", "SELECT foo_col FROM foo_schema.foo_tbl OUTER APPLY foo_schema.fn_bar(foo_tbl.id_col)"),
                Arguments.of("union", "SELECT foo_col FROM foo_schema.foo_tbl UNION SELECT foo_col FROM foo_schema.bar_tbl"),
                Arguments.of("union all", "SELECT foo_col FROM foo_schema.foo_tbl UNION ALL SELECT foo_col FROM foo_schema.bar_tbl"),
                Arguments.of("except", "SELECT foo_col FROM foo_schema.foo_tbl EXCEPT SELECT foo_col FROM foo_schema.bar_tbl"),
                Arguments.of("intersect", "SELECT foo_col FROM foo_schema.foo_tbl INTERSECT SELECT foo_col FROM foo_schema.bar_tbl"),
                Arguments.of("with hint then comma table sources", "SELECT foo_col FROM foo_schema.foo_tbl WITH (NOLOCK), foo_schema.bar_tbl"),
                Arguments.of("inline hint then comma table sources", "SELECT foo_col FROM foo_schema.foo_tbl(NOLOCK), foo_schema.bar_tbl"),
                Arguments.of("block comment then comma table sources", "SELECT foo_col FROM foo_schema.foo_tbl/*hint*/, foo_schema.bar_tbl"));
    }
    
    @Test
    void assertParseDoesNotRejectSingleTableWithHint() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WITH (NOLOCK) WHERE id_col = 1");
        assertThat(actual.getRemainder(), is(" WITH (NOLOCK) WHERE id_col = 1"));
    }
    
    @Test
    void assertRewriteWithDoubleQuotedMultipartTableName() {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM \"foo_schema\".\"foo_tbl\" WHERE id_col = 1");
        String actual = passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", "foo_col_cipher")));
        assertThat(actual, is("SELECT [foo_col_cipher] FROM \"foo_schema\".\"foo_tbl\" WHERE id_col = 1"));
    }
    
    @Test
    void assertRewriteKeepsColumnNameInsideStringLiteral() {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE note_col = 'foo_col'");
        String actual = passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", "foo_col_cipher")));
        assertThat(actual, is("SELECT [foo_col_cipher] FROM foo_schema.foo_tbl WHERE note_col = 'foo_col'"));
    }
    
    @Test
    void assertRewriteWithEncryptedColumnInWhereExpectsException() {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE foo_col IS NOT NULL");
        assertThrows(UnsupportedEncryptSQLException.class, () -> passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", "foo_col_cipher"))));
    }
    
    @Test
    void assertRewriteWithDelimitedEncryptedColumnInWhereExpectsException() {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE [foo_col] IS NOT NULL");
        assertThrows(UnsupportedEncryptSQLException.class, () -> passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", "foo_col_cipher"))));
    }
    
    @Test
    void assertRewriteWithDerivedColumns() {
        EncryptColumn remarkColumn = createEncryptColumn("Remark", "remark_cipher");
        AssistedQueryColumnItem assistedQuery = mock(AssistedQueryColumnItem.class);
        LikeQueryColumnItem likeQuery = mock(LikeQueryColumnItem.class);
        when(assistedQuery.getName()).thenReturn("assisted_query_remark");
        when(likeQuery.getName()).thenReturn("like_query_remark");
        when(remarkColumn.getAssistedQuery()).thenReturn(Optional.of(assistedQuery));
        when(remarkColumn.getLikeQuery()).thenReturn(Optional.of(likeQuery));
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName, Remark FROM foo_schema.foo_tbl WHERE id_col = 4");
        String actual = passThroughSQL.rewrite(Collections.singletonList(remarkColumn));
        assertThat(actual, is("SELECT GroupName, [remark_cipher], [assisted_query_remark], [like_query_remark] FROM foo_schema.foo_tbl WHERE id_col = 4"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("quotedPhysicalColumnNameArguments")
    void assertRewriteQuotesPhysicalColumnName(final String scenario, final String physicalColumnName, final String expectedQuotedColumn) {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE id_col = 1");
        String actual = passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", physicalColumnName)));
        assertThat(actual, is("SELECT " + expectedQuotedColumn + " FROM foo_schema.foo_tbl WHERE id_col = 1"));
    }
    
    @Test
    void assertRewriteRejectsPhysicalColumnNameContainingRightBracket() {
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE id_col = 1");
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> passThroughSQL.rewrite(Collections.singletonList(createEncryptColumn("foo_col", "foo]bar"))));
    }
    
    private static Stream<Arguments> quotedPhysicalColumnNameArguments() {
        return Stream.of(
                Arguments.of("space", "cipher name", "[cipher name]"),
                Arguments.of("reserved word", "order", "[order]"),
                Arguments.of("asterisk", "foo*bar", "[foo*bar]"),
                Arguments.of("slash", "foo/bar", "[foo/bar]"),
                Arguments.of("backslash", "foo\\bar", "[foo\\bar]"),
                Arguments.of("left bracket", "foo[bar", "[foo[bar]"));
    }
    
    private EncryptColumn createEncryptColumn(final String logicColumnName, final String physicalColumnName) {
        EncryptColumn result = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(logicColumnName);
        when(result.getCipher().getName()).thenReturn(physicalColumnName);
        when(result.getAssistedQuery()).thenReturn(Optional.empty());
        when(result.getLikeQuery()).thenReturn(Optional.empty());
        return result;
    }
}
