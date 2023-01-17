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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapperRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationListStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLParseCountAdviceTest extends MetricsAdviceBaseTest {
    
    @After
    public void reset() {
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_insert_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_update_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_delete_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_select_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_ddl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_dcl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_dal_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_tcl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rdl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_ral_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rul_total")).reset();
    }
    
    @Test
    public void assertParseInsertSQL() {
        assertParse("parsed_insert_sql_total", new MySQLInsertStatement());
    }
    
    @Test
    public void assertParseDeleteSQL() {
        assertParse("parsed_delete_sql_total", new MySQLDeleteStatement());
    }
    
    @Test
    public void assertParseUpdateSQL() {
        assertParse("parsed_update_sql_total", new MySQLUpdateStatement());
    }
    
    @Test
    public void assertParseSelectSQL() {
        assertParse("parsed_select_sql_total", new MySQLSelectStatement());
    }
    
    @Test
    public void assertParseDDL() {
        assertParse("parsed_ddl_total", new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertParseDCL() {
        assertParse("parsed_dcl_total", new MySQLCreateUserStatement());
    }
    
    @Test
    public void assertParseDAL() {
        assertParse("parsed_dal_total", new MySQLShowDatabasesStatement());
    }
    
    @Test
    public void assertParseTCL() {
        assertParse("parsed_tcl_total", new MySQLCommitStatement());
    }
    
    @Test
    public void assertParseRQL() {
        assertParse("parsed_rql_total", new ShowStorageUnitsStatement(new DatabaseSegment(0, 0, null), null));
    }
    
    @Test
    public void assertParseRDL() {
        assertParse("parsed_rdl_total", new RegisterStorageUnitStatement(false, Collections.emptyList()));
    }
    
    @Test
    public void assertParseRAL() {
        assertParse("parsed_ral_total", new ShowMigrationListStatement());
    }
    
    @Test
    public void assertParseRUL() {
        assertParse("parsed_rul_total", new FormatStatement("SELECT * FROM t_order"));
    }
    
    private void assertParse(final String metricIds, final SQLStatement sqlStatement) {
        new SQLParseCountAdvice().afterMethod(new MockTargetAdviceObject(), mock(Method.class), new Object[]{}, sqlStatement);
        assertThat(((FixtureWrapper) MetricsWrapperRegistry.get(metricIds)).getFixtureValue(), is(1d));
    }
}
