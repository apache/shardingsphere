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

package org.apache.shardingsphere.test.it.sql.binder.dialect.oracle;

import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderIT;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderITSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SQLBinderITSettings("Oracle")
class OracleBinderIT extends SQLBinderIT {
    
    @Test
    void assertBindRemovedUnparenthesizedFunctionNamesAsDerivedColumns() {
        String sql = "SELECT derived.day, derived.rownum_, derived.row_number "
                + "FROM (SELECT order_id AS day, user_id AS rownum_, status AS row_number FROM t_order) derived";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        List<ProjectionSegment> actualProjections = actual.getProjections().getProjections();
        assertColumnBound(actualProjections.get(0), "t_order", "order_id");
        assertColumnBound(actualProjections.get(1), "t_order", "user_id");
        assertColumnBound(actualProjections.get(2), "t_order", "status");
    }
    
    @Test
    void assertBindQuotedUnparenthesizedFunctionAsDerivedColumn() {
        String sql = "SELECT derived.\"SYSDATE\" FROM (SELECT creation_date AS \"SYSDATE\" FROM t_order) derived";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "t_order", "creation_date");
    }
    
    @Test
    void assertBindOraclePaginationAlias() {
        String sql = "SELECT tt.rownum_ FROM (SELECT ROWNUM rownum_ FROM t_order) tt WHERE tt.rownum_ > 1";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "", "ROWNUM");
    }
    
    @Test
    void assertBindDictionaryViewNameExistingInCurrentSchema() {
        String sql = "SELECT USER_COL FROM ALL_VIEWS";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        ProjectionSegment actualProjection = actual.getProjections().getProjections().get(0);
        assertColumnBound(actualProjection, "ALL_VIEWS", "USER_COL");
        ColumnSegmentBoundInfo actualColumnBoundInfo = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actualColumnBoundInfo.getOriginalSchema().getValue(), is("FOO_DB_1"));
    }
    
    @Test
    void assertBindReadOnlyColumnOnAllTables() {
        String sql = "SELECT READ_ONLY FROM ALL_TABLES";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "ALL_TABLES", "READ_ONLY");
    }
    
    @Test
    void assertBindReadOnlyColumnOnUserTables() {
        String sql = "SELECT READ_ONLY FROM USER_TABLES";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "USER_TABLES", "READ_ONLY");
    }
    
    @Test
    void assertBindDictionaryViewUnderDatabaseIdentifierCaseSensitivity() {
        String sql = "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertColumnBound(actual.getProjections().getProjections().get(0), "ALL_SEQUENCES", "SEQUENCE_NAME");
    }
    
    @Test
    void assertBindDictionaryViewWhenSystemSchemaMetadataAssemblyDisabled() {
        ConfigurationProperties props = new ConfigurationProperties(
                PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED.getKey(), Boolean.FALSE.toString())));
        String sql = "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql, props);
        assertColumnBound(actual.getProjections().getProjections().get(0), "ALL_SEQUENCES", "SEQUENCE_NAME");
    }
    
    @Test
    void assertBindCreateTableWithDictionaryViewName() {
        String sql = "CREATE TABLE ALL_TABLES (FOO_COL NUMBER)";
        CreateTableStatement actual = (CreateTableStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("FOO_DB_1"));
    }
    
    @Test
    void assertBindQuotedLowerCaseDictionaryViewName() {
        String sql = "SELECT SEQUENCE_NAME FROM \"all_sequences\"";
        assertThrows(TableNotFoundException.class, () -> bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties())));
    }
    
    @Test
    void assertBindQuotedDictionaryViewName() {
        String sql = "SELECT SEQUENCE_NAME FROM \"ALL_SEQUENCES\"";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertColumnBound(actual.getProjections().getProjections().get(0), "ALL_SEQUENCES", "SEQUENCE_NAME");
    }
    
    @Test
    void assertBindCreateViewSelectingFromDictionaryView() {
        String sql = "CREATE VIEW v1 AS SELECT READ_ONLY FROM ALL_TABLES";
        CreateViewStatement actual = (CreateViewStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertColumnBound(actual.getSelect().getProjections().getProjections().get(0), "ALL_TABLES", "READ_ONLY");
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"DROP TABLE ALL_TABLES", "ALTER TABLE ALL_TABLES ADD (FOO_COL NUMBER)", "TRUNCATE TABLE ALL_TABLES",
            "CREATE INDEX idx_foo ON ALL_TABLES (OWNER)", "DROP VIEW ALL_SYNONYMS", "ALTER VIEW ALL_SYNONYMS COMPILE"})
    void assertBindDDLTargetWithDictionaryViewName(final String sql) {
        assertThrows(TableNotFoundException.class, () -> bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties())));
    }
    
    @Test
    void assertBindUpdateDBLinkTableSharingDictionaryViewName() {
        String sql = "UPDATE ALL_TABLES@REMOTE_LINK SET USER_COL = 1";
        UpdateStatement actual = (UpdateStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertRemoteTableBound((SimpleTableSegment) actual.getTable());
    }
    
    @Test
    void assertBindInsertDBLinkTableSharingDictionaryViewName() {
        String sql = "INSERT INTO ALL_TABLES@REMOTE_LINK (USER_COL) VALUES (1)";
        InsertStatement actual = (InsertStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertRemoteTableBound(actual.getTable().get());
    }
    
    @Test
    void assertBindDeleteDBLinkTableSharingDictionaryViewName() {
        String sql = "DELETE FROM ALL_TABLES@REMOTE_LINK WHERE USER_COL = 1";
        DeleteStatement actual = (DeleteStatement) bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()));
        assertRemoteTableBound((SimpleTableSegment) actual.getTable());
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"UPDATE ALL_TABLES@REMOTE_LINK SET USER_COL = 1", "INSERT INTO ALL_TABLES@REMOTE_LINK (USER_COL) VALUES (1)", "DELETE FROM ALL_TABLES@REMOTE_LINK WHERE USER_COL = 1"})
    void assertBindDBLinkTableSharingDictionaryViewNameInSystemDatabase(final String sql) {
        assertDoesNotThrow(() -> bindSQLStatement("Oracle", sql, new ConfigurationProperties(new Properties()), "SYS"));
    }
    
    private void assertRemoteTableBound(final SimpleTableSegment actual) {
        assertThat(actual.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("FOO_DB_1"));
    }
    
    private void assertColumnBound(final ProjectionSegment actualProjection, final String expectedTable, final String expectedColumn) {
        assertThat(actualProjection, isA(ColumnProjectionSegment.class));
        ColumnSegmentBoundInfo actual = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actual.getOriginalTable().getValue(), is(expectedTable));
        assertThat(actual.getOriginalColumn().getValue(), is(expectedColumn));
    }
    
    @Test
    void assertBindDottedDatabaseLink() {
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", "SELECT 1 FROM foo_table@foo_link.example.com");
        SimpleTableSegment actualTable = (SimpleTableSegment) actual.getFrom().get();
        assertThat(actualTable.getDbLink().get().getValue(), is("foo_link.example.com"));
        assertThat(actualTable.getAt().get().getValue(), is("@"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypeAttributeStatements")
    void assertBindWithTypeAttribute(final String name, final String sql) {
        SQLStatement actual = bindSQLStatement("Oracle", sql);
        assertTrue(new CommonSQLStatementContext(actual).getTablesContext().getTableNames().isEmpty());
    }
    
    private static Stream<Arguments> getTypeAttributeStatements() {
        return Stream.of(
                Arguments.of("BlockWithLocalRecordFieldTypeAttribute", "DECLARE TYPE foo_rec IS RECORD (order_id NUMBER); t_order foo_rec; foo_copy t_order.order_id%TYPE; BEGIN NULL; END;"),
                Arguments.of("BlockWithLocalCursorRowtypeAttribute", "DECLARE CURSOR foo_cursor IS SELECT 1 foo_id FROM dual; TYPE foo_ref IS REF CURSOR RETURN foo_cursor%ROWTYPE; BEGIN NULL; END;"),
                Arguments.of("PackageWithLocalRecordFieldTypeAttribute",
                        "CREATE PACKAGE foo_pkg AS TYPE foo_rec IS RECORD (foo_id NUMBER); foo_record foo_rec; foo_copy foo_pkg.foo_record.foo_id%TYPE; END;"),
                Arguments.of("PackageWithSchemaQualifiedCursorRowtypeAttribute",
                        "CREATE PACKAGE foo_schema.foo_pkg AS TYPE foo_rec IS RECORD (foo_id NUMBER); CURSOR foo_cursor RETURN foo_rec;"
                                + " TYPE foo_ref IS REF CURSOR RETURN foo_schema.foo_pkg.foo_cursor%ROWTYPE; END;"),
                Arguments.of("PackageWithNestedLocalCursorRowtypeAttribute",
                        "CREATE PACKAGE BODY foo_pkg AS PROCEDURE foo_proc IS CURSOR foo_cursor IS SELECT 1 foo_id FROM dual;"
                                + " TYPE foo_ref IS REF CURSOR RETURN foo_pkg.foo_proc.foo_cursor%ROWTYPE; BEGIN NULL; END; END;"),
                Arguments.of("BlockWithNestedLocalCursorRowtypeAttribute",
                        "<<foo_block>> DECLARE PROCEDURE foo_proc IS CURSOR foo_cursor IS SELECT 1 foo_id FROM dual;"
                                + " TYPE foo_ref IS REF CURSOR RETURN foo_block.foo_proc.foo_cursor%ROWTYPE; BEGIN NULL; END; BEGIN NULL; END;"),
                Arguments.of("PackageWithSchemaQualifiedScalarTypeAttribute", "CREATE PACKAGE foo_schema.foo_pkg AS foo_var NUMBER; foo_copy foo_schema.foo_pkg.foo_var%TYPE; END;"),
                Arguments.of("PackageWithNestedScalarTypeAttribute",
                        "CREATE PACKAGE BODY foo_pkg AS PROCEDURE foo_proc IS foo_var NUMBER; foo_copy foo_pkg.foo_proc.foo_var%TYPE; BEGIN NULL; END; END;"),
                Arguments.of("FunctionWithSchemaQualifiedCursorRowtypeAttribute",
                        "CREATE FUNCTION foo_schema.foo_func RETURN NUMBER AS CURSOR foo_cursor IS SELECT 1 foo_id FROM dual;"
                                + " TYPE foo_ref IS REF CURSOR RETURN foo_schema.foo_func.foo_cursor%ROWTYPE; BEGIN RETURN 1; END;"));
    }
}
