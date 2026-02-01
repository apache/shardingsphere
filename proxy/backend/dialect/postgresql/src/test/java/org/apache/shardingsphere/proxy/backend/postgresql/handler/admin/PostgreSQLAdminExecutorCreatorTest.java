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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLResetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLShowVariableExecutor;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLAdminExecutorCreatorTest {
    
    private static final String PSQL_SELECT_DATABASES = "SELECT d.datname as \"Name\",pg_catalog.pg_get_userbyid(d.datdba) as \"Owner\",pg_catalog.pg_encoding_to_char(d.encoding) as \"Encoding\","
            + "d.datcollate as \"Collate\",d.datctype as \"Ctype\",pg_catalog.array_to_string(d.datacl, E'\\n') AS \"Access privileges\" FROM pg_catalog.pg_database d ORDER BY 1";
    
    private static final String PSQL_SELECT_TABLESPACES = "SELECT spcname AS \"Name\",pg_catalog.pg_get_userbyid(spcowner) AS \"Owner\",pg_catalog.pg_tablespace_location(oid) AS \"Location\""
            + " FROM pg_catalog.pg_tablespace ORDER BY 1";
    
    private static final String SELECT_PG_CATALOG_WITH_SUBQUERY = "SELECT * FROM (SELECT * FROM pg_catalog.pg_namespace) t;";
    
    private static final String SELECT_PG_CLASS_AND_PG_NAMESPACE = "SELECT n.nspname AS \"Schema\",c.relname AS \"Name\","
            + "CASE c.relkind WHEN 'r' THEN 'table' WHEN 'v' THEN 'view' WHEN 'm' THEN 'materialized view' WHEN 'i' THEN 'index' WHEN 'S' THEN 'sequence' WHEN 's' THEN 'special' "
            + "WHEN 'f' THEN 'foreign table' WHEN 'p' THEN 'partitioned table' WHEN 'I' THEN 'partitioned index' END AS \"Type\",pg_catalog.pg_get_userbyid(c.relowner) AS \"Owner\" "
            + "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind IN ('r','p','v','m','S','f','') "
            + "AND n.nspname <> 'pg_catalog' AND n.nspname <> 'information_schema' AND n.nspname !~ '^pg_toast' AND pg_catalog.pg_table_is_visible(c.oid) ORDER BY 1,2;";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertCreateWithSelectDatabase() {
        SQLStatement sqlStatement = parseSQL(PSQL_SELECT_DATABASES);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, PSQL_SELECT_DATABASES, "", Collections.emptyList());
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectTablespace() {
        SQLStatement sqlStatement = parseSQL(PSQL_SELECT_TABLESPACES);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, PSQL_SELECT_TABLESPACES, "", Collections.emptyList());
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectPgCatalogWithSubquery() {
        SQLStatement sqlStatement = parseSQL(SELECT_PG_CATALOG_WITH_SUBQUERY);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, SELECT_PG_CATALOG_WITH_SUBQUERY, "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectPgNamespaceAndPgClass() {
        SQLStatement sqlStatement = parseSQL(SELECT_PG_CLASS_AND_PG_NAMESPACE);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn((SelectStatement) sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(selectStatementContext, SELECT_PG_CLASS_AND_PG_NAMESPACE, "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    private SQLStatement parseSQL(final String sql) {
        CacheOption cacheOption = new CacheOption(0, 0L);
        SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(cacheOption, cacheOption));
        return sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).parse(sql, false);
    }
    
    @Test
    void assertCreateWithSelectNonPgCatalog() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        assertThat(new PostgreSQLAdminExecutorCreator().create(selectStatementContext, "SELECT 1", "", Collections.emptyList()), is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithSetStatement() {
        SetStatement sqlStatement = new SetStatement(databaseType, Collections.emptyList());
        sqlStatement.buildAttributes();
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(sqlStatementContext, "SET client_encoding = utf8", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(PostgreSQLSetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithResetStatement() {
        PostgreSQLResetParameterStatement sqlStatement = new PostgreSQLResetParameterStatement(databaseType, "client_encoding");
        sqlStatement.buildAttributes();
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(new CommonSQLStatementContext(sqlStatement), "RESET client_encoding", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(PostgreSQLResetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithShowSQLStatement() {
        ShowStatement sqlStatement = new ShowStatement(databaseType, "client_encoding");
        sqlStatement.buildAttributes();
        Optional<DatabaseAdminExecutor> actual = new PostgreSQLAdminExecutorCreator().create(new CommonSQLStatementContext(sqlStatement), "SHOW client_encoding", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(PostgreSQLShowVariableExecutor.class));
    }
    
    @Test
    void assertCreateWithDMLStatement() {
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(new DeleteStatement(databaseType));
        assertThat(new PostgreSQLAdminExecutorCreator().create(sqlStatementContext, "DELETE FROM t WHERE id = 1", "", Collections.emptyList()), is(Optional.empty()));
    }
}
