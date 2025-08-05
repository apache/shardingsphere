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

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationListStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
        assertParse(mock(InsertStatement.class), "INSERT=1");
    }
    
    @Test
    void assertParseUpdateSQL() {
        assertParse(mock(UpdateStatement.class), "UPDATE=1");
    }
    
    @Test
    void assertParseDeleteSQL() {
        assertParse(mock(DeleteStatement.class), "DELETE=1");
    }
    
    @Test
    void assertParseSelectSQL() {
        assertParse(mock(SelectStatement.class), "SELECT=1");
    }
    
    @Test
    void assertParseDDL() {
        assertParse(mock(CreateDatabaseStatement.class), "DDL=1");
    }
    
    @Test
    void assertParseDCL() {
        assertParse(mock(CreateUserStatement.class), "DCL=1");
    }
    
    @Test
    void assertParseDAL() {
        assertParse(mock(MySQLShowDatabasesStatement.class), "DAL=1");
    }
    
    @Test
    void assertParseTCL() {
        assertParse(mock(CommitStatement.class), "TCL=1");
    }
    
    @Test
    void assertParseRQL() {
        assertParse(new ShowStorageUnitsStatement(new FromDatabaseSegment(0, new DatabaseSegment(0, 0, null)), null), "RQL=1");
    }
    
    @Test
    void assertParseRDL() {
        assertParse(new RegisterStorageUnitStatement(false, Collections.emptyList(), Collections.emptySet()), "RDL=1");
    }
    
    @Test
    void assertParseRAL() {
        assertParse(new ShowMigrationListStatement(), "RAL=1");
    }
    
    @Test
    void assertParseRUL() {
        assertParse(new ParseStatement("SELECT * FROM tbl"), "RUL=1");
    }
    
    private void assertParse(final SQLStatement sqlStatement, final String expected) {
        new SQLParseCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(TargetAdviceMethod.class), new Object[]{}, sqlStatement, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(config, "FIXTURE").toString(), is(expected));
    }
}
