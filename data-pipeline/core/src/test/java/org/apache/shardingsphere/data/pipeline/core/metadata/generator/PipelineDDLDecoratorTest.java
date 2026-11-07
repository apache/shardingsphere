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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.DialectCommonSQLStatementContextWarpProvider;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineDDLDecoratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertDecorateUsesSchemaSpecificCandidatesForSameNamedTables() {
        String logicIndexName = "very_long_named_index_boundary_case_for_sharding_length_safety_validation";
        String actualIndexName = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_order_0", databaseType);
        assertTrue(actualIndexName.matches(".*_t[0-9a-z]{8}$"));
        String actualSQL = String.format("CREATE INDEX %s ON t_order_0 (order_id)", actualIndexName);
        String expected = String.format("CREATE INDEX %s ON t_order (order_id)", logicIndexName);
        assertThat(
                new PipelineDDLDecorator(mockMetaData(logicIndexName)).decorate(databaseType, "foo_db", "schema_b", "t_order", createParserEngine(actualSQL, actualIndexName), actualSQL).orElse(""),
                is(expected));
    }
    
    private ShardingSphereMetaData mockMetaData(final String logicIndexName) {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema schemaA = new ShardingSphereSchema("schema_a", databaseType, Collections.singleton(
                new ShardingSphereTable("t_order", Collections.emptyList(),
                        Collections.singleton(new ShardingSphereIndex("another_very_long_named_index_for_schema_a_validation", Collections.emptyList(), false)), Collections.emptyList())),
                Collections.emptyList());
        ShardingSphereSchema schemaB = new ShardingSphereSchema("schema_b", databaseType, Collections.singleton(
                new ShardingSphereTable("t_order", Collections.emptyList(),
                        Collections.singleton(new ShardingSphereIndex(logicIndexName, Collections.emptyList(), false)), Collections.emptyList())),
                Collections.emptyList());
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(database.containsSchema("schema_b")).thenReturn(true);
        when(database.getSchema("schema_b")).thenReturn(schemaB);
        when(database.getAllSchemas()).thenReturn(Arrays.asList(schemaA, schemaB));
        return result;
    }
    
    private SQLParserEngine createParserEngine(final String actualSQL, final String actualIndexName) {
        SQLParserEngine result = mock(SQLParserEngine.class);
        when(result.parse(actualSQL, true)).thenReturn(CreateIndexStatement.builder()
                .databaseType(databaseType)
                .index(new IndexSegment(getIndexStartIndex(), getIndexStopIndex(actualIndexName),
                        new IndexNameSegment(getIndexStartIndex(), getIndexStopIndex(actualIndexName), new IdentifierValue(actualIndexName))))
                .table(new SimpleTableSegment(new TableNameSegment(getTableStartIndex(actualSQL), getTableStopIndex(actualSQL), new IdentifierValue("t_order_0"))))
                .build());
        return result;
    }
    
    private int getIndexStartIndex() {
        return "CREATE INDEX ".length();
    }
    
    private int getIndexStopIndex(final String actualIndexName) {
        return getIndexStartIndex() + actualIndexName.length() - 1;
    }
    
    private int getTableStartIndex(final String actualSQL) {
        return actualSQL.indexOf("t_order_0");
    }
    
    private int getTableStopIndex(final String actualSQL) {
        return getTableStartIndex(actualSQL) + "t_order_0".length() - 1;
    }
    
    private SimpleTableSegment createQualifiedTable(final String sql) {
        int tableStartIndex = sql.indexOf("\"UPPER_TABLE\"");
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(tableStartIndex, tableStartIndex + "\"UPPER_TABLE\"".length() - 1, new IdentifierValue("\"UPPER_TABLE\"")));
        int schemaStartIndex = sql.indexOf("\"UPPER_SCHEMA\"");
        result.setOwner(new OwnerSegment(schemaStartIndex, schemaStartIndex + "\"UPPER_SCHEMA\"".length() - 1, new IdentifierValue("\"UPPER_SCHEMA\"")));
        return result;
    }
    
    private ShardingSphereMetaData mockTargetMetaData(final DatabaseIdentifierContext identifierContext) {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(database.getIdentifierContext()).thenReturn(identifierContext);
        return result;
    }
    
    private SQLParserEngine mockParserEngine(final String sql, final SQLStatement sqlStatement) {
        SQLParserEngine result = mock(SQLParserEngine.class);
        when(result.parse(sql, true)).thenReturn(sqlStatement);
        return result;
    }
    
    @Nested
    class CreateTableIdentifiers {
        
        @ParameterizedTest(name = "{0}")
        @CsvSource({"ORDER_ID, order_id", "order_id, order_id", "USER, user"})
        void assertDecorateCreateTable(final String sourceColumnName, final String targetColumnName) {
            String actualSQL = String.format("CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"%s\" bigint, PRIMARY KEY (\"%s\"))", sourceColumnName, sourceColumnName);
            String expected = String.format("CREATE TABLE \"upper_schema\".t_order (\"%s\" bigint, PRIMARY KEY (\"%s\"))", targetColumnName, targetColumnName);
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, createTableStatement(actualSQL, sourceColumnName)), actualSQL).orElse(""), is(expected));
        }
        
        private CreateTableStatement createTableStatement(final String sql, final String columnName) {
            int primaryKeyStartIndex = sql.lastIndexOf('"' + columnName + '"');
            ConstraintDefinitionSegment constraint = mock(ConstraintDefinitionSegment.class);
            when(constraint.getPrimaryKeyColumns()).thenReturn(Collections.singleton(
                    new ColumnSegment(primaryKeyStartIndex, primaryKeyStartIndex + columnName.length() + 1, new IdentifierValue('"' + columnName + '"'))));
            return CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(sql))
                    .columnDefinitions(Collections.singleton(mockColumnDefinition(sql, columnName)))
                    .constraintDefinitions(Collections.singleton(constraint)).build();
        }
        
        private ColumnDefinitionSegment mockColumnDefinition(final String sql, final String columnName) {
            int columnStartIndex = sql.indexOf('"' + columnName + '"');
            ColumnDefinitionSegment result = mock(ColumnDefinitionSegment.class);
            when(result.getColumnName()).thenReturn(new ColumnSegment(columnStartIndex, columnStartIndex + columnName.length() + 1, new IdentifierValue('"' + columnName + '"')));
            return result;
        }
        
        @ParameterizedTest(name = "{0}")
        @CsvSource(quoteCharacter = '~', value = {"foo\"bar", "\"foo\"", "*"})
        void assertDecorateDecodedColumnNames(final String columnName) {
            IdentifierValue identifier = IdentifierValue.fromDecodedQuotedName(columnName);
            String quotedColumnName = identifier.getValueWithQuoteCharacters();
            String actualSQL = "CREATE TABLE foo_table (" + quotedColumnName + " integer, UNIQUE (" + quotedColumnName + "))";
            ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
            when(columnDefinition.getColumnName()).thenReturn(new ColumnSegment(actualSQL.indexOf(quotedColumnName),
                    actualSQL.indexOf(quotedColumnName) + quotedColumnName.length() - 1, identifier));
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(databaseType)
                    .table(new SimpleTableSegment(new TableNameSegment(13, 21, new IdentifierValue("foo_table"))))
                    .columnDefinitions(Collections.singleton(columnDefinition)).build();
            sqlStatement.getColumnReferences().add(new ColumnSegment(actualSQL.lastIndexOf(quotedColumnName),
                    actualSQL.lastIndexOf(quotedColumnName) + quotedColumnName.length() - 1, identifier));
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "public", "foo_table", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""), is(actualSQL));
        }
        
        @Test
        void assertDecorateWholeRowReference() {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"ORDER_ID\" integer, CHECK (\"UPPER_TABLE\".* IS NOT NULL))";
            int referenceStartIndex = actualSQL.lastIndexOf("\"UPPER_TABLE\"");
            ColumnSegment wholeRow = new ColumnSegment(referenceStartIndex, referenceStartIndex + "\"UPPER_TABLE\".*".length() - 1, new IdentifierValue("*"));
            wholeRow.setOwner(new OwnerSegment(referenceStartIndex, referenceStartIndex + "\"UPPER_TABLE\"".length() - 1, new IdentifierValue("\"UPPER_TABLE\"")));
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .columnDefinitions(Collections.singleton(mockColumnDefinition(actualSQL, "ORDER_ID"))).build();
            sqlStatement.getColumnReferences().add(wholeRow);
            String expected = "CREATE TABLE \"upper_schema\".t_order (\"order_id\" integer, CHECK (\"t_order\".* IS NOT NULL))";
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""), is(expected));
        }
        
        @Test
        void assertDecorateWithTargetColumnPolicy() {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"order_id\" bigint, PRIMARY KEY (\"order_id\"))";
            String expected = "CREATE TABLE \"UPPER_SCHEMA\".t_order (\"ORDER_ID\" bigint, PRIMARY KEY (\"ORDER_ID\"))";
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newUpperCasePolicySet())))
                    .decorate(databaseType, "foo_db", "UPPER_SCHEMA", "t_order", mockParserEngine(actualSQL, createTableStatement(actualSQL, "order_id")), actualSQL).orElse(""), is(expected));
        }
        
        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
                "UNIQUE (\"ORDER_ID\")|UNIQUE (\"order_id\")|1",
                "CHECK (\"ORDER_ID\" > 0 AND '\"ORDER_ID\"' <> '')|CHECK (\"order_id\" > 0 AND '\"ORDER_ID\"' <> '')|1",
                "FOREIGN KEY (\"ORDER_ID\") REFERENCES foo_parent (\"ORDER_ID\")|FOREIGN KEY (\"order_id\") REFERENCES foo_parent (\"ORDER_ID\")|1",
                "EXCLUDE USING gist (\"ORDER_ID\" WITH =)|EXCLUDE USING gist (\"order_id\" WITH =)|1"})
        void assertDecorateConstraintReferences(final String constraint, final String expectedConstraint, final int referenceCount) {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"ORDER_ID\" bigint, " + constraint + ")";
            Collection<ColumnSegment> columnReferences = new ArrayList<>(referenceCount);
            int columnStartIndex = actualSQL.indexOf(constraint);
            for (int i = 0; i < referenceCount; i++) {
                columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"", columnStartIndex);
                columnReferences.add(new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\"")));
                columnStartIndex += "\"ORDER_ID\"".length();
            }
            ConstraintDefinitionSegment constraintDefinition = mock(ConstraintDefinitionSegment.class);
            when(constraintDefinition.getColumnReferences()).thenReturn(columnReferences);
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .columnDefinitions(Collections.singleton(mockColumnDefinition(actualSQL, "ORDER_ID"))).constraintDefinitions(Collections.singleton(constraintDefinition)).build();
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE TABLE \"upper_schema\".t_order (\"order_id\" bigint, " + expectedConstraint + ")"));
        }
        
    }
    
    @Nested
    @ExtendWith(AutoMockExtension.class)
    @StaticMockSettings(DatabaseTypedSPILoader.class)
    class OtherDialectIdentifiers {
        
        @Test
        void assertDecorateOtherDialectColumns() {
            DatabaseType openGauss = mock(DatabaseType.class);
            when(openGauss.getType()).thenReturn("openGauss");
            when(DatabaseTypedSPILoader.findService(DialectCommonSQLStatementContextWarpProvider.class, openGauss)).thenReturn(Optional.empty());
            DialectDatabaseMetaData dialectMetaData = mock(DialectDatabaseMetaData.class);
            when((Object) DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, openGauss)).thenReturn(dialectMetaData);
            when(dialectMetaData.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"ORDER_ID\" integer, UNIQUE (\"ORDER_ID\"), CHECK (\"ORDER_ID\" > 0))";
            int columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"");
            ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\" integer".length() - 1,
                    new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\"")), mock(DataTypeSegment.class), false, false,
                    "\"ORDER_ID\" integer");
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(openGauss).table(createQualifiedTable(actualSQL))
                    .columnDefinitions(Collections.singleton(columnDefinition)).build();
            SQLParserEngine parserEngine = mock(SQLParserEngine.class);
            when(parserEngine.parse(actualSQL, true)).thenReturn(sqlStatement);
            assertThat(new PipelineDDLDecorator(mock(ShardingSphereMetaData.class))
                    .decorate(openGauss, "foo_db", "UPPER_SCHEMA", "UPPER_TABLE", parserEngine, actualSQL).orElse(""), is(actualSQL));
        }
    }
    
    @Nested
    class ColumnReferences {
        
        @Test
        void assertDecorateColumnComment() {
            String actualSQL = "COMMENT ON COLUMN \"UPPER_SCHEMA\".\"UPPER_TABLE\".\"ORDER_ID\" IS 'ORDER_ID'";
            int columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"");
            CommentStatement sqlStatement = CommentStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .column(new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\""))).build();
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("COMMENT ON COLUMN \"upper_schema\".t_order.\"order_id\" IS 'ORDER_ID'"));
        }
        
        @Test
        void assertDecorateTableCommentWithoutDatabase() {
            String actualSQL = "COMMENT ON TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" IS 'ORDER_ID'";
            CommentStatement sqlStatement = CommentStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL)).build();
            assertThat(new PipelineDDLDecorator(mock(ShardingSphereMetaData.class))
                    .decorate(databaseType, null, "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("COMMENT ON TABLE \"upper_schema\".t_order IS 'ORDER_ID'"));
        }
        
        @Test
        void assertDecorateIndexColumn() {
            String actualSQL = "CREATE INDEX idx ON \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"ORDER_ID\")";
            int columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"");
            int indexStartIndex = actualSQL.indexOf("idx");
            CreateIndexStatement sqlStatement = CreateIndexStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .index(new IndexSegment(indexStartIndex, indexStartIndex + 2, new IndexNameSegment(indexStartIndex, indexStartIndex + 2, new IdentifierValue("idx"))))
                    .columns(Collections.singleton(new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\"")))).build();
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE INDEX idx ON \"upper_schema\".t_order (\"order_id\")"));
        }
        
        @Test
        void assertDecorateAlterPrimaryKey() {
            String actualSQL = "ALTER TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" ADD PRIMARY KEY (\"ORDER_ID\")";
            int columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"");
            ConstraintDefinitionSegment constraint = mock(ConstraintDefinitionSegment.class);
            when(constraint.getPrimaryKeyColumns()).thenReturn(Collections.singleton(
                    new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\""))));
            AlterTableStatement sqlStatement = AlterTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .addConstraintDefinitions(Collections.singleton(new AddConstraintDefinitionSegment(actualSQL.indexOf("ADD"), actualSQL.length() - 1, constraint))).build();
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("ALTER TABLE \"upper_schema\".t_order ADD PRIMARY KEY (\"order_id\")"));
        }
        
    }
    
    @Nested
    class ForeignKeySchemas {
        
        @ParameterizedTest(name = "{0}")
        @CsvSource({"PARENT_SCHEMA, PARENT_SCHEMA", "UPPER_SCHEMA, target_schema", "upper_schema, upper_schema"})
        void assertDecorateForeignKeySchema(final String referencedSchemaName, final String expectedSchemaName) {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (order_id integer, FOREIGN KEY (order_id) REFERENCES \"" + referencedSchemaName + "\".\"UPPER_TABLE\" (order_id))";
            int tableStartIndex = actualSQL.lastIndexOf("\"UPPER_TABLE\"");
            SimpleTableSegment referencedTable =
                    new SimpleTableSegment(new TableNameSegment(tableStartIndex, tableStartIndex + "\"UPPER_TABLE\"".length() - 1, new IdentifierValue("\"UPPER_TABLE\"")));
            int schemaStartIndex = actualSQL.indexOf("\"" + referencedSchemaName + "\"", actualSQL.indexOf("REFERENCES"));
            referencedTable.setOwner(new OwnerSegment(schemaStartIndex, schemaStartIndex + referencedSchemaName.length() + 1, new IdentifierValue('"' + referencedSchemaName + '"')));
            ConstraintDefinitionSegment constraint = mock(ConstraintDefinitionSegment.class);
            when(constraint.getReferencedTable()).thenReturn(Optional.of(referencedTable));
            CreateTableStatement sqlStatement =
                    CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL)).constraintDefinitions(Collections.singleton(constraint)).build();
            assertThat(new PipelineDDLDecorator(mock(ShardingSphereMetaData.class))
                    .decorate(databaseType, null, "target_schema", "UPPER_TABLE", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE TABLE \"target_schema\".\"UPPER_TABLE\" (order_id integer, FOREIGN KEY (order_id) REFERENCES \"" + expectedSchemaName + "\".\"UPPER_TABLE\" (order_id))"));
        }
    }
    
    @Nested
    class TableExpressions {
        
        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
                "\"ORDER_ID\" integer, \"DERIVED\" integer GENERATED ALWAYS AS (\"ORDER_ID\" + 1) STORED)|\"order_id\" integer, \"derive"
                        + "d\" integer GENERATED ALWAYS AS (\"order_id\" + 1) STORED)|ORDER_ID,DERIVED,ORDER_ID",
                "\"ORDER_ID\" integer) PARTITION BY RANGE (\"ORDER_ID\")|\"order_id\" integer) PARTITION BY RANGE (\"order_id\")|ORDER_"
                        + "ID,ORDER_ID",
                "\"STATUS\" text DEFAULT '\"ORDER_ID\"')|\"status\" text DEFAULT '\"ORDER_ID\"')|STATUS"})
        void assertDecorateTableReferences(final String tableDefinition, final String expectedDefinition, final String referenceNames) {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (" + tableDefinition;
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL)).build();
            int columnStartIndex = 0;
            for (String each : referenceNames.split(",")) {
                columnStartIndex = actualSQL.indexOf('"' + each + '"', columnStartIndex);
                sqlStatement.getColumnReferences().add(new ColumnSegment(columnStartIndex, columnStartIndex + each.length() + 1, new IdentifierValue('"' + each + '"')));
                columnStartIndex += each.length() + 2;
            }
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE TABLE \"upper_schema\".t_order (" + expectedDefinition));
        }
    }
    
    @Nested
    class IndexExpressions {
        
        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
                "(\"ORDER_ID\") INCLUDE (\"STATUS\") WHERE \"STATUS\" <> '\"ORDER_ID\"'|(\"order_id\") INCLUDE (\"status\") WHERE \"status\" "
                        + "<> '\"ORDER_ID\"'|ORDER_ID,STATUS,STATUS",
                "((\"ORDER_ID\" + 1))|((\"order_id\" + 1))|ORDER_ID",
                "(lower(\"STATUS\"))|(lower(\"status\"))|STATUS"})
        void assertDecorateIndexReferences(final String indexDefinition, final String expectedDefinition, final String referenceNames) {
            String actualSQL = "CREATE INDEX foo_idx ON \"UPPER_SCHEMA\".\"UPPER_TABLE\" " + indexDefinition;
            int indexStartIndex = actualSQL.indexOf("foo_idx");
            CreateIndexStatement sqlStatement = CreateIndexStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .index(new IndexSegment(indexStartIndex, indexStartIndex + "foo_idx".length() - 1,
                            new IndexNameSegment(indexStartIndex, indexStartIndex + "foo_idx".length() - 1, new IdentifierValue("foo_idx"))))
                    .build();
            int columnStartIndex = 0;
            for (String each : referenceNames.split(",")) {
                columnStartIndex = actualSQL.indexOf('"' + each + '"', columnStartIndex);
                sqlStatement.getColumnReferences().add(new ColumnSegment(columnStartIndex, columnStartIndex + each.length() + 1, new IdentifierValue('"' + each + '"')));
                columnStartIndex += each.length() + 2;
            }
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "upper_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE INDEX foo_idx ON \"upper_schema\".t_order " + expectedDefinition));
        }
    }
    
    @Nested
    class ForeignKeyColumns {
        
        @ParameterizedTest(name = "{0}.{1}")
        @CsvSource(delimiter = '|', quoteCharacter = '~', value = {
                "PARENT_SCHEMA|PARENT_TABLE|PARENT_ID|\"PARENT_SCHEMA\".\"PARENT_TABLE\" (\"PARENT_ID\")",
                "UPPER_SCHEMA|UPPER_TABLE|ORDER_ID|\"target_schema\".t_order (\"order_id\")",
                "UPPER_SCHEMA|PARENT_TABLE|PARENT_ID|\"UPPER_SCHEMA\".\"PARENT_TABLE\" (\"PARENT_ID\")"})
        void assertDecorateForeignKeyColumns(final String referencedSchemaName, final String referencedTableName, final String referencedColumnName, final String expectedReference) {
            String actualSQL = "CREATE TABLE \"UPPER_SCHEMA\".\"UPPER_TABLE\" (\"ORDER_ID\" integer, FOREIGN KEY (\"ORDER_ID\") REFERENCES \"" + referencedSchemaName
                    + "\".\"" + referencedTableName + "\" (\"" + referencedColumnName + "\"))";
            int tableStartIndex = actualSQL.lastIndexOf('"' + referencedTableName + '"');
            SimpleTableSegment referencedTable = new SimpleTableSegment(new TableNameSegment(tableStartIndex, tableStartIndex + referencedTableName.length() + 1,
                    new IdentifierValue('"' + referencedTableName + '"')));
            int schemaStartIndex = actualSQL.indexOf('"' + referencedSchemaName + '"', actualSQL.indexOf("REFERENCES"));
            referencedTable.setOwner(new OwnerSegment(schemaStartIndex, schemaStartIndex + referencedSchemaName.length() + 1, new IdentifierValue('"' + referencedSchemaName + '"')));
            int localColumnStartIndex = actualSQL.indexOf("\"ORDER_ID\"", actualSQL.indexOf("FOREIGN KEY"));
            int referencedColumnStartIndex = actualSQL.lastIndexOf('"' + referencedColumnName + '"');
            ConstraintDefinitionSegment constraint = mock(ConstraintDefinitionSegment.class);
            when(constraint.getReferencedTable()).thenReturn(Optional.of(referencedTable));
            when(constraint.getColumnReferences()).thenReturn(Collections.singleton(
                    new ColumnSegment(localColumnStartIndex, localColumnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\""))));
            when(constraint.getReferencedColumns()).thenReturn(Collections.singleton(new ColumnSegment(referencedColumnStartIndex,
                    referencedColumnStartIndex + referencedColumnName.length() + 1, new IdentifierValue('"' + referencedColumnName + '"'))));
            ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
            int columnStartIndex = actualSQL.indexOf("\"ORDER_ID\"");
            when(columnDefinition.getColumnName()).thenReturn(new ColumnSegment(columnStartIndex, columnStartIndex + "\"ORDER_ID\"".length() - 1, new IdentifierValue("\"ORDER_ID\"")));
            CreateTableStatement sqlStatement = CreateTableStatement.builder().databaseType(databaseType).table(createQualifiedTable(actualSQL))
                    .columnDefinitions(Collections.singleton(columnDefinition)).constraintDefinitions(Collections.singleton(constraint)).build();
            assertThat(new PipelineDDLDecorator(mockTargetMetaData(new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(databaseType))))
                    .decorate(databaseType, "foo_db", "target_schema", "t_order", mockParserEngine(actualSQL, sqlStatement), actualSQL).orElse(""),
                    is("CREATE TABLE \"target_schema\".t_order (\"order_id\" integer, FOREIGN KEY (\"order_id\") REFERENCES " + expectedReference + ")"));
        }
    }
}
