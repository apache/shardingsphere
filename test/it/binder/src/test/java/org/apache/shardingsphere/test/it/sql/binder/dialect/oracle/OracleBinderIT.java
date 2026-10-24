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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderIT;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderITSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
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
