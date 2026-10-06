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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineSQLSegmentBuilderTest {
    
    private final PipelineSQLSegmentBuilder mysqlBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "MySQL"),
            new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
    
    private final PipelineSQLSegmentBuilder postgresqlBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"),
            new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))));
    
    @Test
    void assertGetEscapedIdentifier() {
        assertThat(mysqlBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, "SELECT"), is("`SELECT`"));
    }
    
    @Test
    void assertGetUnescapedIdentifier() {
        assertThat(mysqlBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, "SELECT1"), is("`SELECT1`"));
    }
    
    @Test
    void assertGetEscapedActualIdentifier() {
        assertThat(postgresqlBuilder.getEscapedActualIdentifier("T_Order"), is("\"T_Order\""));
        assertThat(postgresqlBuilder.getEscapedActualIdentifier("*"), is("*"));
    }
    
    @Test
    void assertGetQualifiedActualTableName() {
        assertThat(postgresqlBuilder.getQualifiedActualTableName("TEST", "T_Order"), is("\"TEST\".\"T_Order\""));
        assertThat(mysqlBuilder.getQualifiedActualTableName("SHARDING_DB", "T_Order"), is("`T_Order`"));
    }
    
    @Test
    void assertGetQualifiedTableNameWithUnsupportedSchema() {
        assertThat(mysqlBuilder.getQualifiedTableName("foo_schema", "foo_tbl"), is("`foo_tbl`"));
        assertThat(mysqlBuilder.getQualifiedTableName(new QualifiedTable("foo_schema", "foo_tbl")), is("`foo_tbl`"));
    }
    
    @Test
    void assertGetQualifiedTableNameWithSupportedSchema() {
        assertThat(postgresqlBuilder.getQualifiedTableName("foo_schema", "foo_tbl"), is("\"foo_schema\".\"foo_tbl\""));
        assertThat(postgresqlBuilder.getQualifiedTableName(new QualifiedTable("foo_schema", "foo_tbl")), is("\"foo_schema\".\"foo_tbl\""));
    }
    
    @Test
    void assertGetQualifiedTableNameWithSupportedSchemaAndNullSchema() {
        assertThat(postgresqlBuilder.getQualifiedTableName(null, "foo_tbl"), is("\"foo_tbl\""));
        assertThat(postgresqlBuilder.getQualifiedTableName(new QualifiedTable(null, "foo_tbl")), is("\"foo_tbl\""));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getScopedIdentifiers")
    void assertGetEscapedIdentifierUsesScope(final String name, final IdentifierScope scope, final String identifier, final String expected) {
        DatabaseIdentifierContext context = mock(DatabaseIdentifierContext.class);
        when(context.normalizeStorage(eq(scope), any())).thenReturn(expected);
        PipelineSQLSegmentBuilder builder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), context);
        assertThat(builder.getEscapedIdentifier(scope, identifier), is("\"" + expected + "\""));
    }
    
    private static Stream<Arguments> getScopedIdentifiers() {
        return Stream.of(Arguments.of("schema", IdentifierScope.SCHEMA, "Tenant", "TENANT"),
                Arguments.of("table", IdentifierScope.TABLE, "Orders", "orders"), Arguments.of("column", IdentifierScope.COLUMN, "OrderId", "OrderId"));
    }
    
    @Test
    void assertGetEscapedIdentifierPreservesQuotedCase() {
        PipelineSQLSegmentBuilder builder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"),
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertThat(builder.getEscapedIdentifier(IdentifierScope.COLUMN, "\"OrderId\""), is("\"OrderId\""));
    }
    
    @Test
    void assertGetEscapedIdentifierPreservesWildcardWithoutContext() {
        PipelineSQLSegmentBuilder builder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        assertThat(builder.getEscapedIdentifier(IdentifierScope.COLUMN, "*"), is("*"));
    }
    
    @Test
    void assertGetQualifiedTableNameWithEmptySchema() {
        assertThat(postgresqlBuilder.getQualifiedTableName("", "Orders"), is("\"orders\""));
    }
    
}
