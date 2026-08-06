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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement.Authorization;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement.Edition;
import org.junit.jupiter.api.Test;

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
    void assertVisitCreatePackage() {
        OracleCreatePackageStatement actual = parseCreatePackage(
                "CREATE OR REPLACE EDITIONABLE PACKAGE IF NOT EXISTS foo_schema.foo_pkg AUTHID CURRENT_USER AS g_public NUMBER := 1; PROCEDURE set_value(p_value IN NUMBER); END foo_pkg;");
        assertThat(actual.getPackageName().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertFalse(actual.isBody());
        assertTrue(actual.isReplace());
        assertTrue(actual.isIfNotExists());
        assertThat(actual.getEdition().get(), is(Edition.EDITIONABLE));
        assertThat(actual.getAuthorization().get(), is(Authorization.CURRENT_USER));
        assertFalse(actual.getInitialization().isPresent());
    }
    
    @Test
    void assertVisitCreatePackageBody() {
        OracleCreatePackageStatement actual = parseCreatePackage("CREATE OR REPLACE NONEDITIONABLE PACKAGE BODY foo_schema.foo_pkg AS"
                + " PROCEDURE set_value(p_value IN NUMBER) AS BEGIN INSERT INTO t_order VALUES (p_value); END;"
                + " BEGIN UPDATE t_order SET order_id = 1; END foo_pkg;");
        assertThat(actual.getPackageName().getIdentifier().getValue(), is("foo_pkg"));
        assertThat(actual.getPackageName().getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertTrue(actual.isBody());
        assertTrue(actual.isReplace());
        assertFalse(actual.isIfNotExists());
        assertThat(actual.getEdition().get(), is(Edition.NONEDITIONABLE));
        assertFalse(actual.getAuthorization().isPresent());
        assertTrue(actual.getInitialization().isPresent());
        assertThat(actual.getSqlStatements().size(), is(2));
    }
    
    private CreateTableStatement parse(final String sql) {
        return (CreateTableStatement) parseStatement(sql);
    }
    
    private OracleCreatePackageStatement parseCreatePackage(final String sql) {
        return (OracleCreatePackageStatement) parseStatement(sql);
    }
    
    private Object parseStatement(final String sql) {
        return new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
}
