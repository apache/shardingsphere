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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.AlterPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.DropPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OraclePLSQLBlockStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement.Authorization;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement.Edition;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.procedure.OracleCreateProcedureStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleDDLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateTableAsSelect() {
        CreateTableStatement actual = parse("CREATE TABLE t_order_new AS SELECT * FROM t_order");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("t_order_new"));
        assertTrue(actual.getColumns().isEmpty());
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    @Test
    void assertVisitCreateTableAsSelectWithExplicitColumnNames() {
        CreateTableStatement actual = parse("CREATE TABLE t_order_new (order_id_new, user_id_new) AS SELECT order_id, user_id FROM t_order");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("t_order_new"));
        assertThat(actual.getColumns().size(), is(2));
        assertThat(actual.getColumns().get(0).getIdentifier().getValue(), is("order_id_new"));
        assertThat(actual.getColumns().get(1).getIdentifier().getValue(), is("user_id_new"));
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    @Test
    void assertVisitCreateTableAsSelectWithIndexOrganizationColumnDefinitions() {
        CreateTableStatement actual = parse("CREATE TABLE admin_iot3(i PRIMARY KEY, j, k, l) ORGANIZATION INDEX PARALLEL AS SELECT * FROM hr.jobs");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("admin_iot3"));
        assertTrue(actual.getColumns().isEmpty());
        assertThat(actual.getColumnDefinitions().size(), is(4));
        assertTrue(actual.getColumnDefinitions().iterator().next().isPrimaryKey());
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    @Test
    void assertVisitCreateTableAsSelectWithClusterClause() {
        CreateTableStatement actual = parse("CREATE TABLE dept_10 CLUSTER personnel (department_id) AS SELECT * FROM employees WHERE department_id = 10");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("dept_10"));
        assertTrue(actual.getColumns().isEmpty());
        assertTrue(actual.getColumnDefinitions().isEmpty());
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    @Test
    void assertVisitCreateViewWithSchemaQualifiedPackageFunction() {
        CreateViewStatement actual = (CreateViewStatement) parseStatement(
                "CREATE OR REPLACE VIEW V_USER AS SELECT SYS.UTL_I18N.RAW_TO_CHAR(PASSWORD, 'AL32UTF8') AS PASSWORD FROM T_USER");
        ExpressionProjectionSegment projection = (ExpressionProjectionSegment) actual.getSelect().getProjections().getProjections().iterator().next();
        FunctionSegment function = (FunctionSegment) projection.getExpr();
        assertThat(function.getFunctionName(), is("RAW_TO_CHAR"));
        assertThat(function.getOwner().getIdentifier().getValue(), is("UTL_I18N"));
        assertThat(function.getOwner().getOwner().get().getIdentifier().getValue(), is("SYS"));
        assertThat(function.getParameters().size(), is(2));
    }
    
    @Test
    void assertVisitCreatePackage() {
        OracleCreatePackageStatement actual = parseCreatePackage(
                "CREATE OR REPLACE EDITIONABLE PACKAGE IF NOT EXISTS foo_schema.foo_pkg AUTHID CURRENT_USER AS g_public NUMBER := 1;"
                        + " PROCEDURE set_value(p_value IN T_USER.USER_ID%TYPE); FUNCTION get_value RETURN T_USER.PASSWORD%TYPE; END foo_pkg;");
        assertThat(actual.getPackageName().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertTrue(actual.getPackageEndName().isPresent());
        assertThat(actual.getPackageEndName().get().getIdentifier().getValue(), is("foo_pkg"));
        assertFalse(actual.isBody());
        assertTrue(actual.isReplace());
        assertTrue(actual.isIfNotExists());
        assertThat(actual.getEdition().get(), is(Edition.EDITIONABLE));
        assertThat(actual.getAuthorization().get(), is(Authorization.CURRENT_USER));
        assertFalse(actual.getInitialization().isPresent());
        Collection<SimpleTableSegment> tables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables();
        assertThat(tables.size(), is(2));
        Iterator<SimpleTableSegment> iterator = tables.iterator();
        assertThat(iterator.next().getTableName().getIdentifier().getValue(), is("T_USER"));
        assertThat(iterator.next().getTableName().getIdentifier().getValue(), is("T_USER"));
    }
    
    @Test
    void assertVisitCreatePackageBody() {
        OracleCreatePackageStatement actual = parseCreatePackage("CREATE OR REPLACE NONEDITIONABLE PACKAGE BODY foo_schema.foo_pkg AS"
                + " PROCEDURE set_value(p_value IN T_USER.USER_ID%TYPE) AS BEGIN INSERT INTO t_order VALUES (p_value); END;"
                + " FUNCTION read_value RETURN T_USER.PASSWORD%TYPE AS BEGIN RETURN NULL; END;"
                + " BEGIN UPDATE t_order SET order_id = 1; END foo_pkg;");
        assertThat(actual.getPackageName().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertTrue(actual.getPackageEndName().isPresent());
        assertThat(actual.getPackageEndName().get().getIdentifier().getValue(), is("foo_pkg"));
        assertTrue(actual.isBody());
        assertTrue(actual.isReplace());
        assertFalse(actual.isIfNotExists());
        assertThat(actual.getEdition().get(), is(Edition.NONEDITIONABLE));
        assertFalse(actual.getAuthorization().isPresent());
        assertTrue(actual.getInitialization().isPresent());
        assertThat(actual.getSqlStatements().size(), is(2));
        Collection<SimpleTableSegment> tables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables();
        assertThat(tables.size(), is(2));
        Iterator<SimpleTableSegment> iterator = tables.iterator();
        assertThat(iterator.next().getTableName().getIdentifier().getValue(), is("T_USER"));
        assertThat(iterator.next().getTableName().getIdentifier().getValue(), is("T_USER"));
    }
    
    @Test
    void assertVisitAlterPackageWithOwner() {
        AlterPackageStatement actual = (AlterPackageStatement) parseStatement("ALTER PACKAGE foo_schema.foo_pkg COMPILE");
        assertTrue(actual.getPackageName().isPresent());
        assertThat(actual.getPackageName().get().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
    }
    
    @Test
    void assertVisitDropPackageBodyWithOwner() {
        DropPackageStatement actual = (DropPackageStatement) parseStatement("DROP PACKAGE BODY foo_schema.foo_pkg");
        assertTrue(actual.getPackageName().isPresent());
        assertThat(actual.getPackageName().get().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertTrue(actual.isBody());
    }
    
    @Test
    void assertVisitAlterFunctionWithOwner() {
        AlterFunctionStatement actual = (AlterFunctionStatement) parseStatement("ALTER FUNCTION foo_schema.foo_func COMPILE");
        assertTrue(actual.getFunctionName().isPresent());
        assertThat(actual.getFunctionName().get().getIdentifier().getValue(), is("foo_func"));
        assertThat(actual.getFunctionName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
    }
    
    @Test
    void assertVisitDropFunctionWithOwner() {
        DropFunctionStatement actual = (DropFunctionStatement) parseStatement("DROP FUNCTION foo_schema.foo_func");
        assertTrue(actual.getFunctionName().isPresent());
        assertThat(actual.getFunctionName().get().getIdentifier().getValue(), is("foo_func"));
        assertThat(actual.getFunctionName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
    }
    
    @Test
    void assertVisitAlterTriggerWithOwner() {
        AlterTriggerStatement actual = (AlterTriggerStatement) parseStatement("ALTER TRIGGER foo_schema.foo_trigger DISABLE");
        assertTrue(actual.getTriggerName().isPresent());
        assertThat(actual.getTriggerName().get().getIdentifier().getValue(), is("foo_trigger"));
        assertThat(actual.getTriggerName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
    }
    
    @Test
    void assertVisitDropTriggerWithOwner() {
        DropTriggerStatement actual = (DropTriggerStatement) parseStatement("DROP TRIGGER foo_schema.foo_trigger");
        assertTrue(actual.getTriggerName().isPresent());
        assertThat(actual.getTriggerName().get().getIdentifier().getValue(), is("foo_trigger"));
        assertThat(actual.getTriggerName().get().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
    }
    
    @Test
    void assertVisitCreatePackageBodyWithProcedureTypeAttributes() {
        String packagePrefix = "CREATE OR REPLACE PACKAGE BODY PKG_E2E_RDL_CONVERT AS";
        String procedurePrefix = " PROCEDURE INSERT_ROW(P_ID IN ";
        String idTypeAttribute = "T_E2E_RDL_CONVERT.ID%TYPE";
        String statusPrefix = ", P_PASSWORD IN VARCHAR2, P_STATUS IN ";
        String statusTypeAttribute = "T_E2E_RDL_CONVERT.STATUS%TYPE";
        String procedureBody = ") IS BEGIN INSERT INTO T_E2E_RDL_CONVERT (ID, PASSWORD, STATUS) VALUES (P_ID, P_PASSWORD, P_STATUS); END INSERT_ROW;";
        String functionPrefix = " FUNCTION READ_PASSWORD(P_ID IN ";
        String functionBody = ") RETURN VARCHAR2 IS V_PASSWORD VARCHAR2(50);"
                + " BEGIN SELECT PASSWORD INTO V_PASSWORD FROM T_E2E_RDL_CONVERT WHERE ID = P_ID; RETURN V_PASSWORD; END READ_PASSWORD; END PKG_E2E_RDL_CONVERT;";
        String sql = packagePrefix + procedurePrefix + idTypeAttribute + statusPrefix + statusTypeAttribute + procedureBody + functionPrefix + idTypeAttribute + functionBody;
        OracleCreatePackageStatement actual = parseCreatePackage(sql);
        assertTrue(actual.isBody());
        Collection<SimpleTableSegment> tables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables();
        assertThat(tables.size(), is(3));
        Iterator<SimpleTableSegment> iterator = tables.iterator();
        int procedureIdTypeStartIndex = packagePrefix.length() + procedurePrefix.length();
        assertTypeAttributeTable(iterator.next(), procedureIdTypeStartIndex);
        int procedureStatusTypeStartIndex = procedureIdTypeStartIndex + idTypeAttribute.length() + statusPrefix.length();
        assertTypeAttributeTable(iterator.next(), procedureStatusTypeStartIndex);
        int functionIdTypeStartIndex = procedureStatusTypeStartIndex + statusTypeAttribute.length() + procedureBody.length() + functionPrefix.length();
        assertTypeAttributeTable(iterator.next(), functionIdTypeStartIndex);
        Collection<ColumnSegment> columns = actual.getColumns();
        assertThat(columns.size(), is(3));
        Iterator<ColumnSegment> columnIterator = columns.iterator();
        procedureIdTypeStartIndex = packagePrefix.length() + procedurePrefix.length();
        assertTypeAttributeColumn(columnIterator.next(), "ID", procedureIdTypeStartIndex);
        procedureStatusTypeStartIndex = procedureIdTypeStartIndex + idTypeAttribute.length() + statusPrefix.length();
        assertTypeAttributeColumn(columnIterator.next(), "STATUS", procedureStatusTypeStartIndex);
        functionIdTypeStartIndex = procedureStatusTypeStartIndex + statusTypeAttribute.length() + procedureBody.length() + functionPrefix.length();
        assertTypeAttributeColumn(columnIterator.next(), "ID", functionIdTypeStartIndex);
    }
    
    @Test
    void assertVisitCreateProcedureWithTypeAttributes() {
        String procedurePrefix = "CREATE OR REPLACE PROCEDURE INSERT_ROW(P_ID IN ";
        String idTypeAttribute = "T_E2E_RDL_CONVERT.ID%TYPE";
        String statusPrefix = ", P_STATUS IN ";
        String statusTypeAttribute = "T_E2E_RDL_CONVERT.STATUS%TYPE";
        String sql = procedurePrefix + idTypeAttribute + statusPrefix + statusTypeAttribute
                + ") IS BEGIN INSERT INTO T_E2E_RDL_CONVERT (ID, STATUS) VALUES (P_ID, P_STATUS); END INSERT_ROW;";
        int idTypeStartIndex = procedurePrefix.length();
        int statusTypeStartIndex = idTypeStartIndex + idTypeAttribute.length() + statusPrefix.length();
        OracleCreateProcedureStatement actual = (OracleCreateProcedureStatement) parseStatement(sql);
        Collection<SimpleTableSegment> tables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables();
        assertThat(tables.size(), is(2));
        Iterator<SimpleTableSegment> iterator = tables.iterator();
        assertTypeAttributeTable(iterator.next(), idTypeStartIndex);
        assertTypeAttributeTable(iterator.next(), statusTypeStartIndex);
        Collection<ColumnSegment> columns = actual.getColumns();
        assertThat(columns.size(), is(2));
        Iterator<ColumnSegment> columnIterator = columns.iterator();
        assertTypeAttributeColumn(columnIterator.next(), "ID", idTypeStartIndex);
        assertTypeAttributeColumn(columnIterator.next(), "STATUS", statusTypeStartIndex);
        assertTrue(actual.getRoutineBody().isPresent());
        assertThat(actual.getRoutineBody().get().getValidStatements().size(), is(1));
    }
    
    @Test
    void assertVisitCreateProcedureWithDMLRoutineBodyTable() {
        String sql = "CREATE OR REPLACE PROCEDURE P_E2E_RDL_SHOW_OBJECT(P_ID IN NUMBER, P_PASSWORD IN VARCHAR2) IS"
                + " BEGIN INSERT INTO T_E2E_RDL_SHOW_OBJECT (ID, PASSWORD) VALUES (P_ID, P_PASSWORD); END;";
        OracleCreateProcedureStatement actual = (OracleCreateProcedureStatement) parseStatement(sql);
        assertTrue(actual.getRoutineBody().isPresent());
        assertThat(actual.getRoutineBody().get().getValidStatements().size(), is(1));
        SimpleTableSegment table = actual.getRoutineBody().get().getValidStatements().iterator().next().getInsert().get().getTable().get();
        assertThat(table.getTableName().getIdentifier().getValue(), is("T_E2E_RDL_SHOW_OBJECT"));
    }
    
    @Test
    void assertVisitCreateProcedureWithInsertSelectRoutineBodyTable() {
        String sql = "CREATE OR REPLACE PROCEDURE std_view_select_insert_user(var_user_id IN NUMBER, var_password IN VARCHAR2) IS"
                + " BEGIN"
                + " EXECUTE IMMEDIATE 'ALTER SESSION SET NLS_DATE_FORMAT = ''YYYY-MM-DD''';"
                + " DELETE FROM t_user;"
                + " INSERT INTO t_user (user_id, user_name, password, email, telephone, creation_date)"
                + " VALUES (100, 'zhangsan', var_password, 'zhangsan@gmail.com', '12345678900', DATE '2017-08-08');"
                + " INSERT INTO t_user (user_id, user_name, password, email, telephone, creation_date)"
                + " SELECT var_user_id, user_name, password, email, telephone, creation_date FROM v_std_user_source WHERE user_id = 100;"
                + " DELETE FROM t_user WHERE user_id = 100;"
                + " END;";
        OracleCreateProcedureStatement actual = (OracleCreateProcedureStatement) parseStatement(sql);
        assertTrue(actual.getRoutineBody().isPresent());
        assertThat(actual.getRoutineBody().get().getValidStatements().size(), is(4));
        Iterator<SimpleTableSegment> tables = new TableExtractor().extractExistTableFromRoutineBody(actual.getRoutineBody().get()).iterator();
        assertThat(tables.next().getTableName().getIdentifier().getValue(), is("t_user"));
        assertThat(tables.next().getTableName().getIdentifier().getValue(), is("t_user"));
        assertThat(tables.next().getTableName().getIdentifier().getValue(), is("v_std_user_source"));
        assertThat(tables.next().getTableName().getIdentifier().getValue(), is("t_user"));
    }
    
    private void assertTypeAttributeTable(final SimpleTableSegment actual, final int expectedStartIndex) {
        assertThat(actual.getTableName().getIdentifier().getValue(), is("T_E2E_RDL_CONVERT"));
        assertThat(actual.getTableName().getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getTableName().getStopIndex(), is(expectedStartIndex + "T_E2E_RDL_CONVERT".length() - 1));
    }
    
    private void assertTypeAttributeColumn(final ColumnSegment actual, final String expectedColumnName, final int expectedStartIndex) {
        assertThat(actual.getIdentifier().getValue(), is(expectedColumnName));
        assertThat(actual.getOwner().get().getIdentifier().getValue(), is("T_E2E_RDL_CONVERT"));
        int expectedColumnStartIndex = expectedStartIndex + "T_E2E_RDL_CONVERT.".length();
        assertThat(actual.getStartIndex(), is(expectedColumnStartIndex));
        assertThat(actual.getStopIndex(), is(expectedColumnStartIndex + expectedColumnName.length() - 1));
    }
    
    @Test
    void assertVisitPLSQLBlockWithProcedureAndFunctionCalls() {
        OraclePLSQLBlockStatement actual = parsePLSQLBlock("BEGIN simple_insert_data(1, 123); ignored := function_insert_data(1, 123); END;");
        List<ProcedureCallNameSegment> procedureCallNames = actual.getProcedureCallNames();
        assertThat(procedureCallNames.size(), is(2));
        assertThat(procedureCallNames.get(0).getIdentifier().getValue(), is("simple_insert_data"));
        assertThat(procedureCallNames.get(1).getIdentifier().getValue(), is("function_insert_data"));
    }
    
    @Test
    void assertVisitCreateTriggerWithDeclarationAndNewReferences() {
        CreateTriggerStatement actual = parseCreateTrigger("/* SHARDINGSPHERE_HINT: TEST=TEST1 */ CREATE OR REPLACE TRIGGER trg_proc_trigger_data\n"
                + "AFTER INSERT ON t_proc_trigger_source\n"
                + "FOR EACH ROW\n"
                + "DECLARE\n"
                + "    new_sensitive INT;\n"
                + "BEGIN\n"
                + "    new_sensitive := :NEW.sensitive;\n"
                + "    DELETE FROM t_proc_trigger_target WHERE id = :NEW.id;\n"
                + "    INSERT INTO t_proc_trigger_target (id, sensitive) VALUES (:NEW.id, new_sensitive);\n"
                + "END;");
        assertThat(actual.getTriggerName().get().getIdentifier().getValue(), is("trg_proc_trigger_data"));
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getTableName().getIdentifier().getValue(), is("t_proc_trigger_source"));
        assertThat(actual.getSqlStatements().size(), is(2));
    }
    
    @Test
    void assertVisitCreateTriggerWithDisableClauseStartIndexAndPseudoColumns() {
        String prefix = "CREATE OR REPLACE TRIGGER trg_proc_trigger_data AFTER INSERT ON t_proc_trigger_source FOR EACH ROW ";
        String whenClause = "WHEN (:NEW.sensitive IS NOT NULL) ";
        String bodyPrefix = "BEGIN ";
        String assignment = ":NEW.sensitive := :OLD.sensitive; END;";
        String sql = prefix + whenClause + bodyPrefix + assignment;
        CreateTriggerStatement actual = parseCreateTrigger(sql);
        assertThat(actual.getDisableClauseStartIndex(), is(prefix.length()));
        assertThat(actual.getTriggerPseudoColumns().size(), is(3));
        Iterator<ColumnSegment> iterator = actual.getTriggerPseudoColumns().iterator();
        int firstNewStartIndex = prefix.length() + "WHEN (".length();
        int secondNewStartIndex = prefix.length() + whenClause.length() + bodyPrefix.length();
        assertTriggerPseudoColumn(iterator.next(), "NEW", "sensitive", firstNewStartIndex);
        assertTriggerPseudoColumn(iterator.next(), "NEW", "sensitive", secondNewStartIndex);
        assertTriggerPseudoColumn(iterator.next(), "OLD", "sensitive", secondNewStartIndex + ":NEW.sensitive := ".length());
    }
    
    @Test
    void assertVisitCreateTriggerWithoutDisableClauseStartIndexForExplicitState() {
        CreateTriggerStatement actual = parseCreateTrigger("CREATE OR REPLACE TRIGGER trg_proc_trigger_data AFTER INSERT ON t_proc_trigger_source FOR EACH ROW DISABLE BEGIN NULL; END;");
        assertThat(actual.getDisableClauseStartIndex(), is(-1));
    }
    
    private void assertTriggerPseudoColumn(final ColumnSegment actual, final String expectedOwner, final String expectedColumn, final int expectedStartIndex) {
        assertThat(actual.getOwner().get().getIdentifier().getValue(), is(expectedOwner));
        assertThat(actual.getIdentifier().getValue(), is(expectedColumn));
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStartIndex + String.format(":%s.%s", expectedOwner, expectedColumn).length() - 1));
    }
    
    private CreateTableStatement parse(final String sql) {
        return (CreateTableStatement) parseStatement(sql);
    }
    
    private OracleCreatePackageStatement parseCreatePackage(final String sql) {
        return (OracleCreatePackageStatement) parseStatement(sql);
    }
    
    private OraclePLSQLBlockStatement parsePLSQLBlock(final String sql) {
        return (OraclePLSQLBlockStatement) parseStatement(sql);
    }
    
    private CreateTriggerStatement parseCreateTrigger(final String sql) {
        return (CreateTriggerStatement) parseStatement(sql);
    }
    
    private Object parseStatement(final String sql) {
        return new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
}
