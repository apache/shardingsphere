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

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.data.pipeline.migration.distsql.statement.queryable.ShowMigrationListStatement;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SQLParseCountAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("parsed_sql_total", MetricCollectorType.COUNTER, null, Collections.singletonList("type"), Collections.emptyMap());
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    void assertParseInsertSQL() {
        assertParse(new MySQLInsertStatement(), "INSERT=1");
    }
    
    @Test
    void assertParseUpdateSQL() {
        assertParse(new MySQLUpdateStatement(), "UPDATE=1");
    }
    
    @Test
    void assertParseDeleteSQL() {
        assertParse(new MySQLDeleteStatement(), "DELETE=1");
    }
    
    @Test
    void assertParseSelectSQL() {
        assertParse(new MySQLSelectStatement(), "SELECT=1");
    }
    
    @Test
    void assertParseDDL() {
        assertParse(new MySQLCreateDatabaseStatement(), "DDL=1");
    }
    
    @Test
    void assertParseDCL() {
        assertParse(new MySQLCreateUserStatement(), "DCL=1");
    }
    
    @Test
    void assertParseDAL() {
        assertParse(new MySQLShowDatabasesStatement(), "DAL=1");
    }
    
    @Test
    void assertParseTCL() {
        assertParse(new MySQLCommitStatement(), "TCL=1");
    }
    
    @Test
    void assertParseRQL() {
        assertParse(new ShowStorageUnitsStatement(new DatabaseSegment(0, 0, null), null), "RQL=1");
    }
    
    @Test
    void assertParseRDL() {
        assertParse(new RegisterStorageUnitStatement(false, Collections.emptyList()), "RDL=1");
    }
    
    @Test
    void assertParseRAL() {
        assertParse(new ShowMigrationListStatement(), "RAL=1");
    }
    
    @Test
    void assertParseRUL() {
        assertParse(new FormatStatement("SELECT * FROM tbl"), "RUL=1");
    }
    
    private void assertParse(final SQLStatement sqlStatement, final String expected) {
        new SQLParseCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(Method.class), new Object[]{}, sqlStatement, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(config, "FIXTURE").toString(), is(expected));
    }
}
