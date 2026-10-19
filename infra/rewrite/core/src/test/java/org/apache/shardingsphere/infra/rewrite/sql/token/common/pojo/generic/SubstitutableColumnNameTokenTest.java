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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class SubstitutableColumnNameTokenTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertToString() {
        Collection<Projection> projections = Collections.singletonList(new ColumnProjection(null, "id", null, databaseType));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, databaseType).toString(mock(RouteUnit.class)), is("id"));
    }
    
    @Test
    void assertToStringWithQuote() {
        Collection<Projection> projections = Collections.singletonList(new ColumnProjection(null,
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), databaseType));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, databaseType).toString(mock(RouteUnit.class)), is("id AS id"));
    }
    
    @Test
    void assertToStringWithOwnerQuote() {
        Collection<Projection> projectionsWithOwnerQuote = Collections.singletonList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.BACK_QUOTE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), databaseType));
        assertThat(new SubstitutableColumnNameToken(0, 1, projectionsWithOwnerQuote, databaseType).toString(mock(RouteUnit.class)), is("temp.id AS id"));
        Collection<Projection> projectionsWithoutOwnerQuote = Collections.singletonList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.NONE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), databaseType));
        assertThat(new SubstitutableColumnNameToken(0, 1, projectionsWithoutOwnerQuote, databaseType).toString(mock(RouteUnit.class)), is("temp.id AS id"));
    }
    
    @Test
    void assertToStringWithSubqueryProjection() {
        Collection<Projection> projections = Arrays.asList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.BACK_QUOTE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), databaseType),
                new SubqueryProjection(new SubqueryProjectionSegment(null, "(SELECT name FROM t_order)"),
                        new ColumnProjection(null, "name", null, databaseType), new IdentifierValue("name"), databaseType));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, databaseType).toString(mock(RouteUnit.class)), is("temp.id AS id, name AS name"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQuotedIdentifiers")
    void assertToStringWithQuotedIdentifiers(final String name, final IdentifierValue owner, final IdentifierValue columnName, final IdentifierValue alias, final Collection<RouteMapper> tableMappers,
                                             final String expected) {
        DialectDatabaseMetaData databaseMetaData = mock(DialectDatabaseMetaData.class);
        when(databaseMetaData.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
        Collection<Projection> projections = Collections.singletonList(new ColumnProjection(owner, columnName, alias, databaseType));
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), tableMappers);
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class,
                        (registry, context) -> when(registry.getDialectDatabaseMetaData()).thenReturn(databaseMetaData))) {
            assertThat(new SubstitutableColumnNameToken(0, 1, projections, databaseType).toString(routeUnit), is(expected));
        }
    }
    
    private static Stream<Arguments> getQuotedIdentifiers() {
        return Stream.of(
                Arguments.of("decoded alias", null, new IdentifierValue("foo_column"), IdentifierValue.fromDecodedQuotedName("foo\"bar"), Collections.emptyList(), "foo_column AS \"foo\"\"bar\""),
                Arguments.of("adjacent decoded alias quotes", null, new IdentifierValue("foo_column"), IdentifierValue.fromDecodedQuotedName("foo\"\"bar"), Collections.emptyList(),
                        "foo_column AS \"foo\"\"\"\"bar\""),
                Arguments.of("decoded column", null, IdentifierValue.fromDecodedQuotedName("foo\"bar"), null, Collections.emptyList(), "\"foo\"\"bar\""),
                Arguments.of("decoded owner without mapping", IdentifierValue.fromDecodedQuotedName("foo\"table"), new IdentifierValue("foo_column"), null, Collections.emptyList(),
                        "\"foo\"\"table\".foo_column"),
                Arguments.of("decoded owner with same-name mapping", IdentifierValue.fromDecodedQuotedName("foo\"table"), new IdentifierValue("foo_column"), null,
                        Collections.singletonList(new RouteMapper("foo\"table", "foo\"table")), "\"foo\"\"table\".foo_column"),
                Arguments.of("renamed owner", IdentifierValue.fromDecodedQuotedName("foo\"table"), new IdentifierValue("foo_column"), null,
                        Collections.singletonList(new RouteMapper("foo\"table", "bar_table")), "\"bar_table\".foo_column"),
                Arguments.of("legacy raw doubled quotes", null, new IdentifierValue("foo\"\"bar", QuoteCharacter.QUOTE), null, Collections.emptyList(), "\"foo\"\"bar\""),
                Arguments.of("back-quote conversion", null, new IdentifierValue("foo_column", QuoteCharacter.BACK_QUOTE), null, Collections.emptyList(), "\"foo_column\""));
    }
}
