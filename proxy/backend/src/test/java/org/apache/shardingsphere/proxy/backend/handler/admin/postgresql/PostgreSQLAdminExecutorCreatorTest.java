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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor.SelectDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLAdminExecutorCreatorTest {
    
    private static final String PSQL_SELECT_DATABASES = "SELECT d.datname as \"Name\",\n"
            + "       pg_catalog.pg_get_userbyid(d.datdba) as \"Owner\",\n"
            + "       pg_catalog.pg_encoding_to_char(d.encoding) as \"Encoding\",\n"
            + "       d.datcollate as \"Collate\",\n"
            + "       d.datctype as \"Ctype\",\n"
            + "       pg_catalog.array_to_string(d.datacl, E'\\n') AS \"Access privileges\"\n"
            + "FROM pg_catalog.pg_database d\n"
            + "ORDER BY 1";
    
    private static final String PSQL_SELECT_TABLESPACES = "SELECT spcname AS \"Name\",\n"
            + "  pg_catalog.pg_get_userbyid(spcowner) AS \"Owner\",\n"
            + "  pg_catalog.pg_tablespace_location(oid) AS \"Location\"\n"
            + "FROM pg_catalog.pg_tablespace\n"
            + "ORDER BY 1";
    
    private static final String SELECT_PG_CATALOG_WITH_SUBQUERY = "select * from (select * from pg_catalog.pg_namespace) t;";
    
    private static final String SELECT_PG_CLASS_AND_PG_NAMESPACE = "SELECT n.nspname as \"Schema\",\n"
            + "       c.relname as \"Name\",\n"
            + "CASE c.relkind WHEN 'r' THEN 'table' WHEN 'v' THEN 'view' WHEN 'm' THEN 'materialized view' WHEN 'i' THEN 'index' WHEN 'S' THEN 'sequence' WHEN 's' THEN 'special' "
            + "WHEN 'f' THEN 'foreign table' WHEN 'p' THEN 'partitioned table' WHEN 'I' THEN 'partitioned index' END as \"Type\",\n"
            + "pg_catalog.pg_get_userbyid(c.relowner) as \"Owner\"\n"
            + "FROM pg_catalog.pg_class c\n"
            + "       LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n"
            + "WHERE c.relkind IN ('r','p','v','m','S','f','')\n"
            + "       AND n.nspname <> 'pg_catalog'\n"
            + "       AND n.nspname <> 'information_schema'\n"
            + "       AND n.nspname !~ '^pg_toast'\n"
            + "       AND pg_catalog.pg_table_is_visible(c.oid)\n"
            + "ORDER BY 1,2;";
    
    @Test
    public void assertCreateWithOtherSQLStatementContextOnly() {
        assertThat(new PostgreSQLAdminExecutorCreator().create(new CommonSQLStatementContext<>(new PostgreSQLInsertStatement())), is(Optional.empty()));
    }
    
    @Test
    public void assertCreateWithShowSQLStatement() {
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(new CommonSQLStatementContext<>(new PostgreSQLShowStatement("client_encoding")));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(PostgreSQLShowVariableExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectDatabase() {
        SQLStatement sqlStatement = parseSQL(PSQL_SELECT_DATABASES);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, PSQL_SELECT_DATABASES, "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SelectDatabaseExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectTablespace() {
        SQLStatement sqlStatement = parseSQL(PSQL_SELECT_TABLESPACES);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, PSQL_SELECT_TABLESPACES, "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SelectTableExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectPgCatalogWithSubquery() {
        SQLStatement sqlStatement = parseSQL(SELECT_PG_CATALOG_WITH_SUBQUERY);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, SELECT_PG_CATALOG_WITH_SUBQUERY, "");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertCreateWithSelectPgNamespaceAndPgClass() {
        SQLStatement sqlStatement = parseSQL(SELECT_PG_CLASS_AND_PG_NAMESPACE);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, SELECT_PG_CLASS_AND_PG_NAMESPACE, "");
        assertFalse(actual.isPresent());
    }
    
    private static SQLStatement parseSQL(final String sql) {
        CacheOption cacheOption = new CacheOption(0, 0);
        SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(false, cacheOption, cacheOption));
        return sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false);
    }
    
    @Test
    public void assertCreateWithSelectNonPgCatalog() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(new PostgreSQLSelectStatement());
        assertThat(new PostgreSQLAdminExecutorCreator().create(selectStatementContext, "select 1", ""), is(Optional.empty()));
    }
    
    @Test
    public void assertCreateWithSetStatement() {
        PostgreSQLSetStatement setStatement = new PostgreSQLSetStatement();
        CommonSQLStatementContext<PostgreSQLSetStatement> sqlStatementContext = new CommonSQLStatementContext<>(setStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(sqlStatementContext, "SET client_encoding = utf8", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(PostgreSQLSetVariableAdminExecutor.class));
    }
    
    @Test
    public void assertCreateWithResetStatement() {
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator()
                .create(new CommonSQLStatementContext<>(new PostgreSQLResetParameterStatement("client_encoding")), "RESET client_encoding", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(PostgreSQLResetVariableAdminExecutor.class));
    }
    
    @Test
    public void assertCreateWithDMLStatement() {
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(new PostgreSQLDeleteStatement());
        assertThat(new PostgreSQLAdminExecutorCreator().create(sqlStatementContext, "delete from t where id = 1", ""), is(Optional.empty()));
    }
    
    @Test
    public void assertGetType() {
        assertThat(new PostgreSQLAdminExecutorCreator().getType(), is("PostgreSQL"));
    }
}
