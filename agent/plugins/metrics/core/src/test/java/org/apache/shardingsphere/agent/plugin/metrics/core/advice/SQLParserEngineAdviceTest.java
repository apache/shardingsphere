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

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
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
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SQLParserEngineAdviceTest extends MetricsAdviceBaseTest {
    
    @Test
    public void assertParseInsertSQL() {
        assertParse(MetricIds.PARSE_SQL_INSERT, new MySQLInsertStatement());
    }
    
    @Test
    public void assertParseDeleteSQL() {
        assertParse(MetricIds.PARSE_SQL_DELETE, new MySQLDeleteStatement());
    }
    
    @Test
    public void assertParseUpdateSQL() {
        assertParse(MetricIds.PARSE_SQL_UPDATE, new MySQLUpdateStatement());
    }
    
    @Test
    public void assertParseSelectSQL() {
        assertParse(MetricIds.PARSE_SQL_SELECT, new MySQLSelectStatement());
    }
    
    @Test
    public void assertParseDDL() {
        assertParse(MetricIds.PARSE_SQL_DDL, new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertParseDCL() {
        assertParse(MetricIds.PARSE_SQL_DCL, new MySQLCreateUserStatement());
    }
    
    @Test
    public void assertParseDAL() {
        assertParse(MetricIds.PARSE_SQL_DAL, new MySQLShowDatabasesStatement());
    }
    
    @Test
    public void assertParseTCL() {
        assertParse(MetricIds.PARSE_SQL_TCL, new MySQLCommitStatement());
    }
    
    @Test
    public void assertParseRQL() {
        assertParse(MetricIds.PARSE_DIST_SQL_RQL, new ShowStorageUnitsStatement(new DatabaseSegment(0, 0, null), null));
    }
    
    @Test
    public void assertParseRDL() {
        assertParse(MetricIds.PARSE_DIST_SQL_RDL, new RegisterStorageUnitStatement(false, Collections.emptyList()));
    }
    
    @Test
    public void assertParseRAL() {
        assertParse(MetricIds.PARSE_DIST_SQL_RAL, new ShowMigrationListStatement());
    }
    
    private void assertParse(final String metricIds, final SQLStatement sqlStatement) {
        MockTargetAdviceObject targetObject = new MockTargetAdviceObject();
        new SQLParserEngineAdvice().afterMethod(targetObject, mock(Method.class), new Object[]{}, sqlStatement);
        assertTrue(MetricsPool.get(metricIds).isPresent());
        assertThat(((FixtureWrapper) MetricsPool.get(metricIds).get()).getFixtureValue(), is(1.0));
    }
}
